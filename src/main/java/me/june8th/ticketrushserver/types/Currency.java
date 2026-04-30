package me.june8th.ticketrushserver.types;

import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Currency {

    USD("USD"), EUR("EUR"), JPY("JPY"), VND("VND");

    @Getter
    private final String value;

    @Nullable
    public static Currency fromString(String value) {
        for (Currency currency : values()) {
            if (currency.value.equalsIgnoreCase(value)) return currency;
        }
        return null;
    }

}
