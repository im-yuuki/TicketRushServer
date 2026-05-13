package me.june8th.ticketrushserver.utils;

public abstract class RandomGenerator {

    static final String RANDOM_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    static final String NUMERIC_CHARACTERS = "0123456789";
    static final int REQUEST_KEY_LENGTH = 32;
    static final int OTP_CODE_LENGTH = 6;

    public static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * RANDOM_CHARACTERS.length());
            sb.append(RANDOM_CHARACTERS.charAt(index));
        }
        return sb.toString();
    }

    public static String generateRandomNumericString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * NUMERIC_CHARACTERS.length());
            sb.append(NUMERIC_CHARACTERS.charAt(index));
        }
        return sb.toString();
    }

    public static String generateRequestKey() {
        return generateRandomString(32);
    }

    public static String generateOtpCode() {
        return generateRandomNumericString(6);
    }

}
