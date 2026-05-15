package me.june8th.ticketrushserver.types;

import lombok.Builder;

import java.util.Optional;

@Builder
public record OperationResult(boolean success, int code, String message, Optional<Long> resourceId) {

    public static OperationResult success(String message) {
        return new OperationResult(true, 200, message, Optional.empty());
    }

    public static OperationResult success(String message, long createdItemId) {
        return new OperationResult(true, 200, message, Optional.of(createdItemId));
    }

    public static OperationResult failure(int code, String message) {
        return new OperationResult(false, code, message, Optional.empty());
    }

}
