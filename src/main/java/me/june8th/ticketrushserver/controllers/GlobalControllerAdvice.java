package me.june8th.ticketrushserver.controllers;

import jakarta.mail.MessagingException;
import me.june8th.ticketrushserver.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalControllerAdvice {

    private static final Logger logger = LoggerFactory.getLogger(GlobalControllerAdvice.class);

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<OperationResponse> handleAuthenticationFailed(AuthenticationFailedException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(OperationResponse.failure("auth_failed", exception.getMessage()));
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<OperationResponse> handleConflict(ResourceConflictException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(OperationResponse.failure("conflict", exception.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<OperationResponse> handleNotFound(ResourceNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(OperationResponse.failure("not_found", exception.getMessage()));
    }

    @ExceptionHandler(RatelimitedException.class)
    public ResponseEntity<OperationResponse> handleRatelimited(RatelimitedException exception) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(OperationResponse.failure("too_many_requests", exception.getMessage()));
    }

    @ExceptionHandler(TimedOutException.class)
    public ResponseEntity<OperationResponse> handleTimedOut(TimedOutException exception) {
        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT)
                .body(OperationResponse.failure("timed_out", exception.getMessage()));
    }

    @ExceptionHandler(InvalidStateException.class)
    public ResponseEntity<OperationResponse> handleInvalidState(InvalidStateException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(OperationResponse.failure("invalid_state", exception.getMessage()));
    }

    @ExceptionHandler(NotImplementedException.class)
    public ResponseEntity<OperationResponse> handleNotImplemented(NotImplementedException exception) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(OperationResponse.failure("not_implemented", exception.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<OperationResponse> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(OperationResponse.failure("bad_request", exception.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<OperationResponse> handleGenericRuntime(RuntimeException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(OperationResponse.failure("server_error", exception.getMessage()));
    }

    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<OperationResponse> handleMessagingException(MessagingException exception) {
        logger.error("Failed to send email to user", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(OperationResponse.failure("email_service_error", "Failed to send email. Please try again later."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<OperationResponse> handleUnexpected(Exception exception) {
        logger.error("Exception throwed from controller", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(OperationResponse.failure("server_error","Unexpected error occurred on server"));
    }
}
