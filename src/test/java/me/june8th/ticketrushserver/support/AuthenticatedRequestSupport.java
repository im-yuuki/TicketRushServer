package me.june8th.ticketrushserver.support;

import me.june8th.ticketrushserver.controllers.ErrorHandler;
import me.june8th.ticketrushserver.security.AccessTokenProvider;
import me.june8th.ticketrushserver.utils.ClientIPResolver;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public abstract class AuthenticatedRequestSupport {

    public static final String TEST_JWT_SECRET = "test-secret-key-test-secret-key-1234";

    public static AccessTokenProvider accessTokenProvider() {
        return new AccessTokenProvider(TEST_JWT_SECRET, 3600);
    }

    public static MockMvc buildMockMvc(Object controller, ClientIPResolver clientIPResolver) {
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ErrorHandler(clientIPResolver))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

}
