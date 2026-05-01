package me.june8th.ticketrushserver.types;

import lombok.Builder;

import java.util.HashMap;

@Builder
public record OperationResponse (boolean success, String message, HashMap<String, String> metadata) {

    public OperationResponse(boolean success, String message) {
        this(success, message, new HashMap<>());
    }

    public static OperationResponse success(String message) {
        return new OperationResponse(true, message, new HashMap<>());
    }

    public static OperationResponse failure(String message) {
        return new OperationResponse(false, message, new HashMap<>());
    }

    public void addMetadataEntry(String key, String value) {
        metadata.put(key, value);
    }

}
