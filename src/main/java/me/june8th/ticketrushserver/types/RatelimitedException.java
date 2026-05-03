package me.june8th.ticketrushserver.types;

public class RatelimitedException extends RuntimeException {

    public RatelimitedException(String message) {
        super(message);
    }

}
