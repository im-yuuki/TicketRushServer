package me.june8th.ticketrushserver.types;

import lombok.Builder;

@Builder
public record OperationResult(boolean success, int code, String message) {

    public static OperationResult success(String message) {
        return new OperationResult(true, 200, message);
    }

    public static OperationResult failure(int code, String message) {
        return new OperationResult(false, code, message);
    }

}
