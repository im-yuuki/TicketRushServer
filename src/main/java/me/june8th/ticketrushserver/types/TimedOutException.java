package me.june8th.ticketrushserver.types;

public class TimedOutException extends RuntimeException {

    public TimedOutException(String message) {
        super(message);
    }

}
