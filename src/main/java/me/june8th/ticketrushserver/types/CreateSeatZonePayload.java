package me.june8th.ticketrushserver.types;


public record CreateSeatZonePayload(String name, int positionX, int positionY, long capacity, SeatRowView[] rows) {

    public record SeatRowView(int index, String label, SeatView[] seats) {

        public record SeatView(int index, int number) {}

    }

}
