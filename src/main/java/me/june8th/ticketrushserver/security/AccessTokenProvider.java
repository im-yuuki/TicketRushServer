package me.june8th.ticketrushserver.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import me.june8th.ticketrushserver.data.Account;
import me.june8th.ticketrushserver.types.AccountType;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class AccessTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(AccessTokenProvider.class);

    private static final String ISSUER = AccessTokenProvider.class.getPackageName();
    private static final String ID_CLAIM = "id";
    private static final String TYPE_CLAIM = "type";
    private static final String DOMAIN_CLAIM = "domain";
    private static final String VERSION_CLAIM = "version";

    private final SecretKey secretKey;
    private final long accessTokenExpiration;

    public AccessTokenProvider(@Value("${app.jwt.secret}") String secretKey, @Value("${app.jwt.access-token-expiration}") long accessTokenExpiration) {
        if (secretKey.isEmpty()) {
            logger.warn("JWT secret key isn't set. Using randomly generated key.");
            this.secretKey = Jwts.SIG.HS256.key().build();
        } else {
            this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        }
        this.accessTokenExpiration = accessTokenExpiration * 1000; // Convert to milliseconds
    }

    public String generateAccessToken(Account account) {
        return generateAccessToken(AccessTokenData.builder()
                .id(account.getId())
                .type(account.getType())
                .domain(account.getDomain())
                .version(account.getTokenVersion())
                .build());
    }

    public String generateAccessToken(AccessTokenData data) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .subject(data.getPrincipal())
                .claim(ID_CLAIM, data.id())
                .claim(TYPE_CLAIM, data.type())
                .claim(DOMAIN_CLAIM, data.domain())
                .claim(VERSION_CLAIM, data.version())
                .issuer(ISSUER)
                .issuedAt(now)
                .notBefore(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    @Nullable
    public AccessTokenData parseAccessToken(String token) {
        if (token == null || token.isEmpty()) return null;

        Date now = new Date();

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            if (now.before(claims.getIssuedAt()) || now.before(claims.getNotBefore())) {
                logger.debug("Invalid token");
            }
            else if (now.after(claims.getExpiration())) {
                logger.debug("Token expired at: {}", claims.getExpiration());
            }
            else if (!ISSUER.equals(claims.getIssuer())) {
                logger.debug("Invalid token issuer: {}", claims.getIssuer());
            }
            else {
                Long id = claims.get(ID_CLAIM, Long.class);
                AccountType type = claims.get(TYPE_CLAIM, AccountType.class);
                String domain = claims.get(DOMAIN_CLAIM, String.class);
                Integer version = claims.get(VERSION_CLAIM, Integer.class);

                logger.debug("Parsed access token ID: {}, Type: {}, Domain: {}, Version: {}", id, type, domain, version);
                AccessTokenData data = new AccessTokenData(id, type, domain, version);

                if (!data.getPrincipal().equals(claims.getSubject())) {
                    logger.debug("Token subject is compromised.");
                } else {
                    return data;
                }
            }
        } catch (Exception e) {
            logger.debug("Failed to parse access token: {}", e.getMessage());
        }
        return null;
    }

}

