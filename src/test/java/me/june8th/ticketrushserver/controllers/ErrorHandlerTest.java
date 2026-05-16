package me.june8th.ticketrushserver.controllers;

import me.june8th.ticketrushserver.types.AuthenticationFailedException;
import me.june8th.ticketrushserver.types.InvalidStateException;
import me.june8th.ticketrushserver.types.RatelimitedException;
import me.june8th.ticketrushserver.types.ResourceConflictException;
import me.june8th.ticketrushserver.types.ResourceNotFoundException;
import me.june8th.ticketrushserver.utils.ClientIPResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ErrorHandlerTest {

    private final MockMvc mockMvc;

    private final ClientIPResolver clientIPResolver;

    ErrorHandlerTest(@org.mockito.Mock ClientIPResolver clientIPResolver) {
        this.clientIPResolver = clientIPResolver;
        this.mockMvc = MockMvcBuilders.standaloneSetup(new ThrowingController())
                .setControllerAdvice(new ErrorHandler(clientIPResolver))
                .build();
    }

    @BeforeEach
    void setUp() {
        when(clientIPResolver.resolve(any())).thenReturn("127.0.0.1");
    }

    @Test
    void authenticationFailed_shouldReturn401() throws Exception {
        mockMvc.perform(get("/test/errors/authentication"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("bad credentials"));
    }

    @Test
    void resourceConflict_shouldReturn409() throws Exception {
        mockMvc.perform(get("/test/errors/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("already exists"));
    }

    @Test
    void resourceNotFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/test/errors/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("missing"));
    }

    @Test
    void ratelimited_shouldReturn429() throws Exception {
        mockMvc.perform(get("/test/errors/ratelimited"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("slow down"));
    }

    @Test
    void illegalArgument_shouldReturn400() throws Exception {
        mockMvc.perform(get("/test/errors/bad-request"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("bad input"));
    }

    @Test
    void maxUploadSizeExceeded_shouldReturn400() throws Exception {
        mockMvc.perform(get("/test/errors/upload-too-large"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Uploaded file is too large"));
    }

    @Test
    void unexpectedException_shouldReturn500() throws Exception {
        mockMvc.perform(get("/test/errors/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Unexpected error occurred on server"));
    }

    @RestController
    static class ThrowingController {

        @GetMapping("/test/errors/authentication")
        void authentication() {
            throw new AuthenticationFailedException("bad credentials");
        }

        @GetMapping("/test/errors/conflict")
        void conflict() {
            throw new ResourceConflictException("already exists");
        }

        @GetMapping("/test/errors/not-found")
        void notFound() {
            throw new ResourceNotFoundException("missing");
        }

        @GetMapping("/test/errors/ratelimited")
        void ratelimited() {
            throw new RatelimitedException("slow down");
        }

        @GetMapping("/test/errors/bad-request")
        void badRequest() {
            throw new IllegalArgumentException("bad input");
        }

        @GetMapping("/test/errors/unexpected")
        void unexpected() throws Exception {
            throw new Exception("boom");
        }

        @GetMapping("/test/errors/upload-too-large")
        void uploadTooLarge() {
            throw new MaxUploadSizeExceededException(20L * 1024 * 1024);
        }

        @GetMapping("/test/errors/invalid-state")
        void invalidState() {
            throw new InvalidStateException("invalid");
        }
    }

}
