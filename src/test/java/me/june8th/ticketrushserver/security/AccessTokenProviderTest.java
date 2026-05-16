package me.june8th.ticketrushserver.security;

import me.june8th.ticketrushserver.types.AccessTokenData;
import me.june8th.ticketrushserver.types.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class AccessTokenProviderTest {

    private static final String SECRET = "test-secret-key-test-secret-key-1234";

    @Test
    void generateAndParse_shouldRoundTripClaims() {
        AccessTokenProvider provider = new AccessTokenProvider(SECRET, 3600);
        AccessTokenData data = AccessTokenData.builder()
                .id(42L)
                .role(Role.USER)
                .domain("42")
                .version(3)
                .build();

        String token = provider.generateAccessToken(data);
        AccessTokenData parsed = provider.parseAccessToken(token);

        assertNotNull(token);
        assertEquals(data, parsed);
    }

    @Test
    void parse_shouldReturnNullForEmptyToken() {
        AccessTokenProvider provider = new AccessTokenProvider(SECRET, 3600);

        assertNull(provider.parseAccessToken(""));
    }

    @Test
    void parse_shouldReturnNullForNullToken() {
        AccessTokenProvider provider = new AccessTokenProvider(SECRET, 3600);

        assertNull(provider.parseAccessToken(null));
    }

    @Test
    void parse_shouldReturnNullForWrongSecret() {
        AccessTokenProvider writer = new AccessTokenProvider(SECRET, 3600);
        AccessTokenProvider reader = new AccessTokenProvider("other-secret-key-other-secret-key-5678", 3600);
        String token = writer.generateAccessToken(AccessTokenData.builder()
                .id(1L)
                .role(Role.USER)
                .domain("1")
                .version(0)
                .build());

        assertNull(reader.parseAccessToken(token));
    }

    @Test
    void parse_shouldReturnNullForMalformedToken() {
        AccessTokenProvider provider = new AccessTokenProvider(SECRET, 3600);

        assertNull(provider.parseAccessToken("not-a-token"));
    }

}
