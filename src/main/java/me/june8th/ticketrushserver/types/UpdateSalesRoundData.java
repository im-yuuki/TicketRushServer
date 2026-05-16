package me.june8th.ticketrushserver.types;

import me.june8th.ticketrushserver.data.SalesRound;
import me.june8th.ticketrushserver.utils.Validator;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

public record UpdateSalesRoundData(
        Optional<String> name,
        Optional<Instant> startTime,
        Optional<Instant> endTime,
        Optional<Integer> maxTicketsPerPurchase
) {
    public SalesRound patchSalesRound(SalesRound salesRound) {
        Validator validator = new Validator();
        name.ifPresent(name -> {
            validator.validateName(name);
            salesRound.setName(name);
        });
        startTime.ifPresent(startTime -> {
            validator.validateFutureDate(Date.from(startTime));
            salesRound.setStartTime(startTime);
        });
        endTime.ifPresent(endTime -> {
            validator.validateFutureDate(Date.from(endTime));
            salesRound.setEndTime(endTime);
        });
        maxTicketsPerPurchase.ifPresent(max -> {
            validator.validateNaturalNumber(max);
            salesRound.setMaxTicketsPerPurchase(max);
        });
        validator.throwExceptionIfInvalid();
        return salesRound;
    }
}
