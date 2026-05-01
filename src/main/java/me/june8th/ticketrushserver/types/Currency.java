package me.june8th.ticketrushserver.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Currency {

    USD("USD"), EUR("EUR"), JPY("JPY"), VND("VND");

    @Getter
    private final String value;

    @JsonCreator
    public static Currency fromString(String value) {
        for (Currency currency : values()) {
            if (currency.value.equalsIgnoreCase(value) || currency.name().equalsIgnoreCase(value)) {
                return currency;
            }
        }
        throw new IllegalArgumentException("Invalid gender: " + value);
    }

}
