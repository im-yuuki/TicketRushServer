package me.june8th.ticketrushserver.controllers;

import lombok.RequiredArgsConstructor;
import me.june8th.ticketrushserver.services.PurchaseService;
import me.june8th.ticketrushserver.types.OperationResult;
import me.june8th.ticketrushserver.types.PurchaseData.CompletedPurchaseView;
import me.june8th.ticketrushserver.types.PurchaseData.HoldItemRequest;
import me.june8th.ticketrushserver.types.PurchaseData.HoldView;
import me.june8th.ticketrushserver.types.PurchaseData.PurchaseEventView;
import me.june8th.ticketrushserver.types.PurchaseData.SeatStatusCollectionView;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/purchase")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    @GetMapping("/event/{eventId}")
    public ResponseEntity<PurchaseEventView> getPurchaseEvent(@AuthenticationPrincipal long userId, @PathVariable long eventId) {
        return ResponseEntity.ok(purchaseService.getPurchaseEvent(userId, eventId));
    }

    @GetMapping("/event/{eventId}/seats/status")
    public ResponseEntity<SeatStatusCollectionView> getSeatStatuses(@AuthenticationPrincipal long userId, @PathVariable long eventId) {
        return ResponseEntity.ok(purchaseService.getSeatStatuses(userId, eventId));
    }

    @PostMapping("/hold")
    public ResponseEntity<HoldView> createHold(@AuthenticationPrincipal long userId, @RequestBody CreateHoldPayload payload) {
        return ResponseEntity.ok(purchaseService.createHold(userId, payload.eventId()));
    }

    @PostMapping("/hold/{holdId}/seat")
    public ResponseEntity<HoldView> addSeatToHold(@AuthenticationPrincipal long userId, @PathVariable String holdId, @RequestBody HoldSeatPayload payload) {
        return ResponseEntity.ok(purchaseService.addSeatToHold(userId, holdId, new HoldItemRequest(payload.seatId(), payload.ticketClassId())));
    }

    @GetMapping("/hold/{holdId}")
    public ResponseEntity<HoldView> getHold(@AuthenticationPrincipal long userId, @PathVariable String holdId) {
        return ResponseEntity.ok(purchaseService.getHold(userId, holdId));
    }

    @DeleteMapping("/hold/{holdId}")
    public ResponseEntity<OperationResult> releaseHold(@AuthenticationPrincipal long userId, @PathVariable String holdId) {
        purchaseService.releaseHold(userId, holdId);
        return ResponseEntity.ok(OperationResult.success("Seat hold released successfully"));
    }

    @PostMapping("/pay/{holdId}")
    public ResponseEntity<CompletedPurchaseView> completeMockPayment(@AuthenticationPrincipal long userId, @PathVariable String holdId) {
        return ResponseEntity.ok(purchaseService.completeMockPayment(userId, holdId));
    }

    public record CreateHoldPayload(long eventId) {}

    public record HoldSeatPayload(long seatId, long ticketClassId) {}

}
