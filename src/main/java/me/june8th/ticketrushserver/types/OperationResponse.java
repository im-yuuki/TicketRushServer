package me.june8th.ticketrushserver.types;

import lombok.Data;

import java.util.HashMap;

@Data
public class OperationResponse {

    private final boolean success;
    private final String message;
    private final HashMap<String, String> metadata = new HashMap<>();

    public static OperationResponse success(String message) {
        return new OperationResponse(true, message);
    }

    public static OperationResponse failure(String message) {
        return new OperationResponse(false, message);
    }

    public void addMetadataEntry(String key, String value) {
        metadata.put(key, value);
    }

}
