package me.june8th.ticketrushserver.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.june8th.ticketrushserver.data.Account;
import me.june8th.ticketrushserver.database.AccountRepository;
import me.june8th.ticketrushserver.types.AuthenticationFailedException;
import me.june8th.ticketrushserver.utils.CookieUtils;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.authentication.ott.OneTimeTokenAuthentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.FactorGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationFilter extends OncePerRequestFilter {

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
                    log.trace("Account {} token version mismatch: {} != {}", account.getId(), account.getTokenVersion(), accessTokenData.version());
                    throw new AuthenticationFailedException("Invalid token");
                }
                if (!Objects.equals(account.getRole(), accessTokenData.role())) {
                    log.trace("Account {} role mismatch: {} != {}", account.getId(), account.getRole(), accessTokenData.role());
                    throw new AuthenticationFailedException("Invalid token");
                }
                if (!Objects.equals(account.getDomain(), accessTokenData.domain())) {
                    log.trace("Account {} domain mismatch: {} != {}", account.getId(), account.getDomain(), accessTokenData.domain());
                    throw new AuthenticationFailedException("Invalid token");
                }
                Collection<GrantedAuthority> authorities = new ArrayList<>();
                authorities.add(FactorGrantedAuthority.fromAuthority(FactorGrantedAuthority.PASSWORD_AUTHORITY));
                authorities.add(accessTokenData.role().toRoleAuthority());
                OneTimeTokenAuthentication authentication = new OneTimeTokenAuthentication(accessTokenData.id(), authorities);
                authentication.setDetails(accessTokenData);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Authenticated principal {} with role {}", account.getId(), accessTokenData.role());
            }
        } catch (Exception e) {
            log.error("Could not set user authentication in security context", e);
        } finally {
            filterChain.doFilter(request, response);
        }
    }

}

