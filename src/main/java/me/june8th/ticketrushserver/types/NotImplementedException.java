package me.june8th.ticketrushserver.types;

public class NotImplementedException extends RuntimeException {

    public NotImplementedException() {
        super("Method not implemented");
    }

    public NotImplementedException(String message) {
        super(message);
    }
}
