package me.june8th.ticketrushserver.views;

import lombok.Builder;

@Builder
public record OperationResponse (boolean success, int code, String message) {

    public static OperationResponse success(String message) {
        return new OperationResponse(true, 200, message);
    }

    public static OperationResponse failure(int code, String message) {
        return new OperationResponse(false, code, message);
    }

}
