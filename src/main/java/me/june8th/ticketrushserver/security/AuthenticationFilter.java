package me.june8th.ticketrushserver.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.june8th.ticketrushserver.data.Account;
import me.june8th.ticketrushserver.repositories.AccountRepository;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
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

    private final AccessTokenProvider accessTokenProvider;
    private final AccountRepository accountRepository;

    @Override
    @NullMarked
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            AccessTokenData accessTokenData = accessTokenProvider.parseAccessToken(extractCookie(request));
            if (accessTokenData != null) {
                Account account = accountRepository.findById(accessTokenData.id()).orElseThrow(
                        () -> new RuntimeException("Account not found")
                );
                // compare token data with account
                if (!Objects.equals(account.getTokenVersion(), accessTokenData.version())) {
                    throw new RuntimeException("Invalid token version");
                }
                if (!Objects.equals(account.getType(), accessTokenData.type())) {
                    throw new RuntimeException("Invalid account type");
                }
                if (!Objects.equals(account.getDomain(), accessTokenData.domain())) {
                    throw new RuntimeException("Invalid token domain");
                }
                OneTimeTokenAuthentication authentication = new OneTimeTokenAuthentication(
                        accessTokenData.getPrincipal(),
                        Collections.singleton(new SimpleGrantedAuthority(accessTokenData.type().toString()))
                );
                authentication.setDetails(accessTokenData.domain());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("Could not set user authentication in security context", e);
        } finally {
            filterChain.doFilter(request, response);
        }
    }

    @Nullable
    private String extractCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) for (Cookie cookie : cookies) {
            if ("accessToken".equals(cookie.getName())) return cookie.getValue();
        }
        return null;
    }

}

