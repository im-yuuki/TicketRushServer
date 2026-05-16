package me.june8th.ticketrushserver.controllers;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.june8th.ticketrushserver.types.*;
import me.june8th.ticketrushserver.utils.ClientIPResolver;
import me.june8th.ticketrushserver.types.OperationResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class ErrorHandler {

    private final ClientIPResolver clientIPResolver;

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<OperationResult> handleAuthenticationFailed(AuthenticationFailedException exception, HttpServletRequest request) {
        writeLog("Authentication failed", exception, request);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(OperationResult.failure(exception.getMessage()));
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<OperationResult> handleConflict(ResourceConflictException exception, HttpServletRequest request) {
        writeLog("Resource conflict", exception, request);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(OperationResult.failure(exception.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<OperationResult> handleNotFound(ResourceNotFoundException exception, HttpServletRequest request) {
        writeLog("Resource not found", exception, request);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(OperationResult.failure(exception.getMessage()));
    }

    @ExceptionHandler(RatelimitedException.class)
    public ResponseEntity<OperationResult> handleRatelimited(RatelimitedException exception, HttpServletRequest request) {
        writeLog("Rate limited", exception, request);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(OperationResult.failure(exception.getMessage()));
    }

    @ExceptionHandler(TimedOutException.class)
    public ResponseEntity<OperationResult> handleTimedOut(TimedOutException exception, HttpServletRequest request) {
        writeLog("Request timed out", exception, request);
        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(OperationResult.failure(exception.getMessage()));
    }

    @ExceptionHandler(InvalidStateException.class)
    public ResponseEntity<OperationResult> handleInvalidState(InvalidStateException exception, HttpServletRequest request) {
        writeLog("Invalid state", exception, request);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(OperationResult.failure(exception.getMessage()));
    }

    @ExceptionHandler(NotImplementedException.class)
    public ResponseEntity<OperationResult> handleNotImplemented(NotImplementedException exception, HttpServletRequest request) {
        writeLog("Not implemented", exception, request);
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(OperationResult.failure(exception.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<OperationResult> handleIllegalArgument(IllegalArgumentException exception, HttpServletRequest request) {
        writeLog("Bad request", exception, request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(OperationResult.failure(exception.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<OperationResult> handleGenericRuntime(RuntimeException exception, HttpServletRequest request) {
        writeLog("Unhandled runtime exception", exception, request);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(OperationResult.failure(exception.getMessage()));
    }

    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<OperationResult> handleMessagingException(MessagingException exception, HttpServletRequest request) {
        log.error("Failed to send email to user on {}", request.getRequestURI(), exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(OperationResult.failure("Failed to send email. Please try again later."));
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<OperationResult> handleIOException(IOException exception, HttpServletRequest request) {
        log.error("IO error occurred during request processing {}", request.getRequestURI(), exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(OperationResult.failure("An internal error occurred while processing the request. Please try again later."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<OperationResult> handleUnexpected(Exception exception, HttpServletRequest request) {
        log.error("Exception throwed from {}", request.getRequestURI(), exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(OperationResult.failure("Unexpected error occurred on server"));
    }

    private void writeLog(String message, Exception exception, HttpServletRequest request) {
        String origin = request.getHeader("Origin");
        String path = request.getRequestURI();
        String clientIp = clientIPResolver.resolve(request);
        log.debug("{} [ip={}, origin={}, path={}]", message, clientIp, origin, path, exception);
    }

}
