package me.june8th.ticketrushserver.types;


public record SeatZoneData(long id, String name, int positionX, int positionY, long capacity, SeatRowView[] rows) {

    public record SeatRowView(long id, int index, String label, SeatView[] seats) {

        public record SeatView(long id, int index, int number) {}

    }

}
