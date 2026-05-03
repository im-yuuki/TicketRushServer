package me.june8th.ticketrushserver.types;

import lombok.Builder;

import java.util.HashMap;

@Builder
public record OperationResponse (
        boolean success,
        String code,
        String message,
        HashMap<String, String> metadata) {

    public OperationResponse(boolean success, String code, String message) {
        this(success, code, message, new HashMap<>());
    }

    public static OperationResponse success(String message) {
        return new OperationResponse(true, "success", message, new HashMap<>());
    }

    public static OperationResponse failure(String code, String message) {
        return new OperationResponse(false, code, message, new HashMap<>());
    }

    public OperationResponse addMetadataEntry(String key, String value) {
        metadata.put(key, value);
        return this;
    }

}
