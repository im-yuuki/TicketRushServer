package me.june8th.ticketrushserver.types;

import me.june8th.ticketrushserver.data.Event;
import me.june8th.ticketrushserver.utils.Validator;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

public record UpdateEventPayload(
        Optional<String> name,
        Optional<String> description,
        Optional<Boolean> isOnlineEvent,
        Optional<String> venue,
        Optional<String> address,
        Optional<Instant> dateTime
) {

    public Event patchEvent(Event event) {
        Validator validator = new Validator();
        name.ifPresent(name -> {
            validator.validateName(name);
            event.setName(name);
        });
        description.ifPresent(description -> {
            validator.validateNotBlank(description);
            event.setDescription(description);
        });
        isOnlineEvent.ifPresent(event::setOnlineEvent);
        venue.ifPresent(venue -> {
            validator.validateName(venue);
            event.setVenue(venue);
        });
        address.ifPresent(address -> {
            validator.validateNotBlank(address);
            event.setAddress(address);
        });
        dateTime.ifPresent(dateTime -> {
            validator.validateFutureDate(Date.from(dateTime));
            event.setDateTime(dateTime);
        });
        validator.throwExceptionIfInvalid();
        return event;
    }

}
