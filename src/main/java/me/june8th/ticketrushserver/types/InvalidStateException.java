package me.june8th.ticketrushserver.types;

public class InvalidStateException extends RuntimeException {

    public InvalidStateException(String message) {
        super(message);
    }

}
