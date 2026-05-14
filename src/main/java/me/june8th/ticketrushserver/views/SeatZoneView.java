package me.june8th.ticketrushserver.views;

import org.jspecify.annotations.Nullable;

public record SeatZoneView(@Nullable Long id, String name, int positionX, int positionY, @Nullable Long capacity, SeatRowView[] rows) {

    public record SeatRowView(@Nullable Long id, int index, String label, SeatView[] seats) {

        public record SeatView(@Nullable Long id, int index, int number) {}

    }

}
