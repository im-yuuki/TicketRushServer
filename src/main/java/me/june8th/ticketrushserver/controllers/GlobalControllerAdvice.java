package me.june8th.ticketrushserver.controllers;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import me.june8th.ticketrushserver.types.*;
import me.june8th.ticketrushserver.utils.ClientIPResolver;
import me.june8th.ticketrushserver.views.OperationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private static final Logger logger = LoggerFactory.getLogger(GlobalControllerAdvice.class);

    private final ClientIPResolver clientIPResolver;

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<OperationResponse> handleAuthenticationFailed(AuthenticationFailedException exception, HttpServletRequest request) {
        logTrace("Authentication failed", exception, request);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(OperationResponse.failure(HttpStatus.UNAUTHORIZED.value(), exception.getMessage()));
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<OperationResponse> handleConflict(ResourceConflictException exception, HttpServletRequest request) {
        logTrace("Resource conflict", exception, request);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(OperationResponse.failure(HttpStatus.CONFLICT.value(), exception.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<OperationResponse> handleNotFound(ResourceNotFoundException exception, HttpServletRequest request) {
        logTrace("Resource not found", exception, request);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(OperationResponse.failure(HttpStatus.NOT_FOUND.value(), exception.getMessage()));
    }

    @ExceptionHandler(RatelimitedException.class)
    public ResponseEntity<OperationResponse> handleRatelimited(RatelimitedException exception, HttpServletRequest request) {
        logTrace("Rate limited", exception, request);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(OperationResponse.failure(HttpStatus.TOO_MANY_REQUESTS.value(), exception.getMessage()));
    }

    @ExceptionHandler(TimedOutException.class)
    public ResponseEntity<OperationResponse> handleTimedOut(TimedOutException exception, HttpServletRequest request) {
        logTrace("Request timed out", exception, request);
        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT)
                .body(OperationResponse.failure(HttpStatus.REQUEST_TIMEOUT.value(), exception.getMessage()));
    }

    @ExceptionHandler(InvalidStateException.class)
    public ResponseEntity<OperationResponse> handleInvalidState(InvalidStateException exception, HttpServletRequest request) {
        logTrace("Invalid state", exception, request);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(OperationResponse.failure(HttpStatus.CONFLICT.value(), exception.getMessage()));
    }

    @ExceptionHandler(NotImplementedException.class)
    public ResponseEntity<OperationResponse> handleNotImplemented(NotImplementedException exception, HttpServletRequest request) {
        logTrace("Not implemented", exception, request);
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(OperationResponse.failure(HttpStatus.NOT_IMPLEMENTED.value(), exception.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<OperationResponse> handleIllegalArgument(IllegalArgumentException exception, HttpServletRequest request) {
        logTrace("Bad request", exception, request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(OperationResponse.failure(HttpStatus.BAD_REQUEST.value(), exception.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<OperationResponse> handleGenericRuntime(RuntimeException exception, HttpServletRequest request) {
        logTrace("Unhandled runtime exception", exception, request);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(OperationResponse.failure(HttpStatus.INTERNAL_SERVER_ERROR.value(), exception.getMessage()));
    }

    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<OperationResponse> handleMessagingException(MessagingException exception, HttpServletRequest request) {
        logTrace("Messaging exception", exception, request);
        logger.error("Failed to send email to user", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(OperationResponse.failure(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to send email. Please try again later."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<OperationResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
        logTrace("Unexpected exception", exception, request);
        logger.error("Exception throwed from controller", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(OperationResponse.failure(HttpStatus.INTERNAL_SERVER_ERROR.value(),"Unexpected error occurred on server"));
    }

    private void logTrace(String message, Exception exception, HttpServletRequest request) {
        String origin = request.getHeader("Origin");
        String path = request.getRequestURI();
        String clientIp = clientIPResolver.resolve(request);
        logger.trace("{} [ip={}, origin={}, path={}]", message, clientIp, origin, path, exception);
    }

}
