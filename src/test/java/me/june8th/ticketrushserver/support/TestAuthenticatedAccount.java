package me.june8th.ticketrushserver.support;

import jakarta.servlet.http.Cookie;
import lombok.Builder;
import me.june8th.ticketrushserver.data.Account;
import me.june8th.ticketrushserver.data.AdministratorAccount;
import me.june8th.ticketrushserver.data.Event;
import me.june8th.ticketrushserver.data.EventStaffAccount;
import me.june8th.ticketrushserver.data.OrganizationAccount;
import me.june8th.ticketrushserver.data.UserAccount;
import me.june8th.ticketrushserver.security.AccessTokenProvider;
import me.june8th.ticketrushserver.types.AccessTokenData;
import me.june8th.ticketrushserver.types.Gender;
import me.june8th.ticketrushserver.types.Role;
import me.june8th.ticketrushserver.utils.CookieUtils;
import org.springframework.security.authentication.ott.OneTimeTokenAuthentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.FactorGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

@Builder
public record TestAuthenticatedAccount(
        long id,
        String name,
        String email,
        Role role,
        String domain,
        int tokenVersion,
        String avatarKey
) {

    private static final String ID_ENV = "TEST_AUTH_ACCOUNT_ID";
    private static final String NAME_ENV = "TEST_AUTH_ACCOUNT_NAME";
    private static final String EMAIL_ENV = "TEST_AUTH_ACCOUNT_EMAIL";
    private static final String ROLE_ENV = "TEST_AUTH_ACCOUNT_ROLE";
    private static final String DOMAIN_ENV = "TEST_AUTH_ACCOUNT_DOMAIN";
    private static final String TOKEN_VERSION_ENV = "TEST_AUTH_ACCOUNT_TOKEN_VERSION";
    private static final String AVATAR_KEY_ENV = "TEST_AUTH_ACCOUNT_AVATAR_KEY";

    public static TestAuthenticatedAccount fromEnvironment() {
        long id = parseLong(System.getenv(ID_ENV), 1L);
        Role role = Role.fromString(System.getenv().getOrDefault(ROLE_ENV, Role.USER.name()));
        String defaultDomain = role == Role.STAFF ? "100" : String.valueOf(id);
        return TestAuthenticatedAccount.builder()
                .id(id)
                .name(System.getenv().getOrDefault(NAME_ENV, "Test User"))
                .email(System.getenv().getOrDefault(EMAIL_ENV, "test.user@example.com"))
                .role(role)
                .domain(System.getenv().getOrDefault(DOMAIN_ENV, defaultDomain))
                .tokenVersion((int) parseLong(System.getenv(TOKEN_VERSION_ENV), 0L))
                .avatarKey(emptyToNull(System.getenv(AVATAR_KEY_ENV)))
                .build();
    }

    public AccessTokenData toAccessTokenData() {
        return AccessTokenData.builder()
                .id(id)
                .role(role)
                .domain(domain)
                .version(tokenVersion)
                .build();
    }

    public Cookie accessTokenCookie(AccessTokenProvider accessTokenProvider) {
        return new Cookie(CookieUtils.ACCESS_TOKEN_COOKIE_NAME, accessTokenProvider.generateAccessToken(toAccessTokenData()));
    }

    public OneTimeTokenAuthentication authentication() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(FactorGrantedAuthority.fromAuthority(FactorGrantedAuthority.PASSWORD_AUTHORITY));
        authorities.add(role.toRoleAuthority());
        OneTimeTokenAuthentication authentication = new OneTimeTokenAuthentication(id, authorities);
        authentication.setDetails(toAccessTokenData());
        return authentication;
    }

    public RequestPostProcessor requestPostProcessor() {
        return request -> {
            OneTimeTokenAuthentication authentication = authentication();
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            request.setUserPrincipal(authentication);
            return request;
        };
    }

    public Account toAccount() {
        return switch (role) {
            case USER -> UserAccount.builder()
                    .id(id)
                    .name(name)
                    .email(email)
                    .passwordHash("hashed")
                    .tokenVersion(tokenVersion)
                    .avatarKey(avatarKey)
                    .birthDate(new Date(946684800000L))
                    .gender(Gender.OTHER)
                    .build();
            case ORGANIZATION -> OrganizationAccount.builder()
                    .id(id)
                    .name(name)
                    .email(email)
                    .passwordHash("hashed")
                    .tokenVersion(tokenVersion)
                    .avatarKey(avatarKey)
                    .build();
            case ADMINISTRATOR -> AdministratorAccount.builder()
                    .id(id)
                    .name(name)
                    .email(email)
                    .passwordHash("hashed")
                    .tokenVersion(tokenVersion)
                    .build();
            case STAFF -> EventStaffAccount.builder()
                    .id(id)
                    .name(name)
                    .email(email)
                    .passwordHash("hashed")
                    .tokenVersion(tokenVersion)
                    .event(Event.builder()
                            .id(parseLong(domain, 100L))
                            .name("Test Event")
                            .organization(OrganizationAccount.builder()
                                    .id(200L)
                                    .name("Test Org")
                                    .email("org@example.com")
                                    .passwordHash("hashed")
                                    .build())
                            .isOnlineEvent(false)
                            .venue("Venue")
                            .address("Address")
                            .dateTime(Instant.now().plusSeconds(3600))
                            .build())
                    .build();
        };
    }

    private static long parseLong(String value, long defaultValue) {
        if (value == null || value.isBlank()) return defaultValue;
        return Long.parseLong(value);
    }

    private static String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

}
