package me.june8th.ticketrushserver.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Country {

    US("us", "United States"),
    JP("jp", "Japan"),
    VN("vn", "Vietnam");

    @Getter
    private final String code;

    @Getter
    private final String name;

    @JsonCreator
    public static Country fromString(String value) {
        for (Country country : values()) {
            if (country.code.equalsIgnoreCase(value) || country.name.equalsIgnoreCase(value)) {
                return country;
            }
        }
        throw new IllegalArgumentException("Invalid country: " + value);
    }

    @Override
    public String toString() {
        return code;
    }

}
