package me.june8th.ticketrushserver.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.june8th.ticketrushserver.data.Account;
import me.june8th.ticketrushserver.repositories.AccountRepository;
import me.june8th.ticketrushserver.types.AuthenticationFailedException;
import me.june8th.ticketrushserver.utils.CookieUtils;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.ott.OneTimeTokenAuthentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class AuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

    private final AccessTokenProvider accessTokenProvider;
    private final AccountRepository accountRepository;

    @Override
    @NullMarked
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            AccessTokenData accessTokenData = accessTokenProvider.parseAccessToken(
                    CookieUtils.getCookie(request, CookieUtils.ACCESS_TOKEN_COOKIE_NAME)
            );
            if (accessTokenData != null) {
                Account account = accountRepository.findById(accessTokenData.id()).orElseThrow(
                        () -> new AuthenticationFailedException("Account not found")
                );
                // compare token data with account
                if (!Objects.equals(account.getTokenVersion(), accessTokenData.version())) {
                    logger.trace("Account {} token version mismatch: {} != {}", account.getId(), account.getTokenVersion(), accessTokenData.version());
                    throw new AuthenticationFailedException("Invalid token");
                }
                if (!Objects.equals(account.getType(), accessTokenData.type())) {
                    logger.trace("Account {} type mismatch: {} != {}", account.getId(), account.getType(), accessTokenData.type());
                    throw new AuthenticationFailedException("Invalid token");
                }
                if (!Objects.equals(account.getDomain(), accessTokenData.domain())) {
                    logger.trace("Account {} domain mismatch: {} != {}", account.getId(), account.getDomain(), accessTokenData.domain());
                    throw new AuthenticationFailedException("Invalid token");
                }
                OneTimeTokenAuthentication authentication = new OneTimeTokenAuthentication(
                        accessTokenData.id(),
                        Collections.singleton(accessTokenData.type().toSecurityAuthority())
                );
                authentication.setDetails(accessTokenData);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.debug("Authenticated principal {} with authority {}", account.getId(), accessTokenData.type().toSecurityAuthority());
            }
        } catch (Exception e) {
            logger.error("Could not set user authentication in security context", e);
        } finally {
            filterChain.doFilter(request, response);
        }
    }

}

