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
import me.june8th.ticketrushserver.security.AccessTokenData;
import me.june8th.ticketrushserver.types.Gender;
import me.june8th.ticketrushserver.types.Role;
import me.june8th.ticketrushserver.utils.CookieUtils;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;
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
import java.util.Properties;

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

    private static final String CONFIG_PREFIX = "test.auth.account.";
    private static final TestAuthenticatedAccount DEFAULT_ACCOUNT = TestAuthenticatedAccount.builder()
            .id(42L)
            .name("Test User")
            .email("test.user@example.com")
            .role(Role.USER)
            .domain("42")
            .tokenVersion(0)
            .avatarKey("avatars/test-user.png")
            .build();

    public static TestAuthenticatedAccount fromApplicationTestConfig() {
        Properties properties = loadProperties();
        long id = parseLong(properties.getProperty(CONFIG_PREFIX + "id"), DEFAULT_ACCOUNT.id());
        Role role = Role.fromString(properties.getProperty(CONFIG_PREFIX + "role", DEFAULT_ACCOUNT.role().name()));
        String defaultDomain = role == Role.STAFF ? "100" : String.valueOf(id);
        return TestAuthenticatedAccount.builder()
                .id(id)
                .name(properties.getProperty(CONFIG_PREFIX + "name", DEFAULT_ACCOUNT.name()))
                .email(properties.getProperty(CONFIG_PREFIX + "email", DEFAULT_ACCOUNT.email()))
                .role(role)
                .domain(properties.getProperty(CONFIG_PREFIX + "domain", defaultDomain))
                .tokenVersion((int) parseLong(properties.getProperty(CONFIG_PREFIX + "token-version"), DEFAULT_ACCOUNT.tokenVersion()))
                .avatarKey(emptyToNull(properties.getProperty(CONFIG_PREFIX + "avatar-key", DEFAULT_ACCOUNT.avatarKey())))
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

    private static Properties loadProperties() {
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource("application-test.yml"));
        Properties properties = yaml.getObject();
        return properties == null ? new Properties() : properties;
    }

    private static String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

}
