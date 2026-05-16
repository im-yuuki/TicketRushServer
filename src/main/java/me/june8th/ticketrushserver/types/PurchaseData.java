package me.june8th.ticketrushserver.types;

import java.time.Instant;
import java.util.List;

public final class PurchaseData {

    private PurchaseData() {}

    public record HoldItemRequest(long seatId, long ticketClassId) {}

    public record PurchaseEventView(
            long eventId,
            String eventName,
            Instant eventDateTime,
            List<SalesRoundView> salesRounds,
            List<TicketClassView> ticketClasses,
            List<SeatZoneView> seatZones,
            ActiveHoldView myActiveHold
    ) {}

    public record SalesRoundView(long id, String name, Instant startTime, Instant endTime, int maxTicketsPerPurchase) {}

    public record TicketClassView(long id, String name, String description, long price, long salesRoundId, long seatZoneId) {}

    public record SeatZoneView(long id, String name, int positionX, int positionY, List<SeatRowView> rows) {}

    public record SeatRowView(long id, int index, String label, List<SeatView> seats) {}

    public record SeatView(long id, int index, int number, String availability) {}

    public record SeatStatusCollectionView(long eventId, List<SeatZoneView> seatZones, ActiveHoldView myActiveHold) {}

    public record ActiveHoldView(String holdId, Instant expiresAt, long totalAmount) {}

    public record HoldView(String holdId, Instant expiresAt, long totalAmount, List<HeldItemView> items) {}

    public record HeldItemView(long seatId, long ticketClassId, long price) {}

    public record CompletedPurchaseView(long purchaseId, long amount, List<Long> ticketIds) {}

    public record UserTicketView(
            long ticketId,
            Instant purchasedAt,
            long purchaseId,
            long eventId,
            String eventName,
            Instant eventDateTime,
            String salesRoundName,
            String ticketClassName,
            String seatZoneName,
            String seatRowLabel,
            int seatNumber,
            String ticketSecretCode,
            Instant checkedInAt
    ) {}

    public record HoldCart(String holdId, long userId, long eventId, Instant expiresAt, long totalAmount, List<HoldCartItem> items) {}

    public record HoldCartSnapshot(String rawJson, HoldCart holdCart) {}

    public record HoldCartItem(long seatId, long ticketClassId, long price) {}

    public record MockPaymentDetails(String type, String holdId, Instant paidAt, long amount, List<HoldCartItem> items) {}

}
