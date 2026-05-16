package me.june8th.ticketrushserver.types;

import lombok.Builder;

import java.util.Optional;

@Builder
public record OperationResult(boolean success, String message, Optional<Long> resourceId) {

    public static OperationResult success(String message) {
        return new OperationResult(true, message, Optional.empty());
    }

    public static OperationResult success(String message, long createdItemId) {
        return new OperationResult(true, message, Optional.of(createdItemId));
    }

    public static OperationResult failure(String message) {
        return new OperationResult(false, message, Optional.empty());
    }

}
