package me.june8th.ticketrushserver.controllers;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import me.june8th.ticketrushserver.types.*;
import me.june8th.ticketrushserver.utils.ClientIPResolver;
import me.june8th.ticketrushserver.types.OperationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class ErrorHandler {

    private static final Logger logger = LoggerFactory.getLogger(ErrorHandler.class);

    private final ClientIPResolver clientIPResolver;

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<OperationResult> handleAuthenticationFailed(AuthenticationFailedException exception, HttpServletRequest request) {
        log("Authentication failed", exception, request);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(OperationResult.failure(HttpStatus.UNAUTHORIZED.value(), exception.getMessage()));
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<OperationResult> handleConflict(ResourceConflictException exception, HttpServletRequest request) {
        log("Resource conflict", exception, request);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(OperationResult.failure(HttpStatus.CONFLICT.value(), exception.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<OperationResult> handleNotFound(ResourceNotFoundException exception, HttpServletRequest request) {
        log("Resource not found", exception, request);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(OperationResult.failure(HttpStatus.NOT_FOUND.value(), exception.getMessage()));
    }

    @ExceptionHandler(RatelimitedException.class)
    public ResponseEntity<OperationResult> handleRatelimited(RatelimitedException exception, HttpServletRequest request) {
        log("Rate limited", exception, request);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(OperationResult.failure(HttpStatus.TOO_MANY_REQUESTS.value(), exception.getMessage()));
    }

    @ExceptionHandler(TimedOutException.class)
    public ResponseEntity<OperationResult> handleTimedOut(TimedOutException exception, HttpServletRequest request) {
        log("Request timed out", exception, request);
        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT)
                .body(OperationResult.failure(HttpStatus.REQUEST_TIMEOUT.value(), exception.getMessage()));
    }

    @ExceptionHandler(InvalidStateException.class)
    public ResponseEntity<OperationResult> handleInvalidState(InvalidStateException exception, HttpServletRequest request) {
        log("Invalid state", exception, request);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(OperationResult.failure(HttpStatus.CONFLICT.value(), exception.getMessage()));
    }

    @ExceptionHandler(NotImplementedException.class)
    public ResponseEntity<OperationResult> handleNotImplemented(NotImplementedException exception, HttpServletRequest request) {
        log("Not implemented", exception, request);
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(OperationResult.failure(HttpStatus.NOT_IMPLEMENTED.value(), exception.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<OperationResult> handleIllegalArgument(IllegalArgumentException exception, HttpServletRequest request) {
        log("Bad request", exception, request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(OperationResult.failure(HttpStatus.BAD_REQUEST.value(), exception.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<OperationResult> handleGenericRuntime(RuntimeException exception, HttpServletRequest request) {
        log("Unhandled runtime exception", exception, request);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(OperationResult.failure(HttpStatus.INTERNAL_SERVER_ERROR.value(), exception.getMessage()));
    }

    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<OperationResult> handleMessagingException(MessagingException exception, HttpServletRequest request) {
        log("Messaging exception", exception, request);
        logger.error("Failed to send email to user", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(OperationResult.failure(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to send email. Please try again later."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<OperationResult> handleUnexpected(Exception exception, HttpServletRequest request) {
        log("Unexpected exception", exception, request);
        logger.error("Exception throwed from controller", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(OperationResult.failure(HttpStatus.INTERNAL_SERVER_ERROR.value(),"Unexpected error occurred on server"));
    }

    private void log(String message, Exception exception, HttpServletRequest request) {
        String origin = request.getHeader("Origin");
        String path = request.getRequestURI();
        String clientIp = clientIPResolver.resolve(request);
        logger.debug("{} [ip={}, origin={}, path={}]", message, clientIp, origin, path, exception);
    }

}
