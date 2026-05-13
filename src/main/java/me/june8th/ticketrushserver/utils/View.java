package me.june8th.ticketrushserver.utils;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder
public abstract class View {

    // can be read by everyone who have access to the endpoint
    public interface Public {}

    // can be read by whom releated to the data
    public interface Shared extends Public {}

    // only read by owner
    public interface Private extends Shared {}

}

