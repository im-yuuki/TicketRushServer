package me.june8th.ticketrushserver.controllers;

import lombok.RequiredArgsConstructor;
import me.june8th.ticketrushserver.services.PurchaseService;
import me.june8th.ticketrushserver.types.OperationResult;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/purchase")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    @GetMapping("/event/{eventId}")
    public ResponseEntity<PurchaseService.PurchaseEventView> getPurchaseEvent(@AuthenticationPrincipal long userId, @PathVariable long eventId) {
        return ResponseEntity.ok(purchaseService.getPurchaseEvent(userId, eventId));
    }

    @PostMapping("/hold")
    public ResponseEntity<PurchaseService.HoldView> createHold(@AuthenticationPrincipal long userId, @RequestBody CreateHoldPayload payload) {
        List<PurchaseService.HoldItemRequest> items = payload.items() == null ? null : payload.items().stream().map(item -> new PurchaseService.HoldItemRequest(item.seatId(), item.ticketClassId())).toList();
        return ResponseEntity.ok(purchaseService.createHold(userId, payload.eventId(), items));
    }

    @GetMapping("/hold/{holdId}")
    public ResponseEntity<PurchaseService.HoldView> getHold(@AuthenticationPrincipal long userId, @PathVariable String holdId) {
        return ResponseEntity.ok(purchaseService.getHold(userId, holdId));
    }

    @DeleteMapping("/hold/{holdId}")
    public ResponseEntity<OperationResult> releaseHold(@AuthenticationPrincipal long userId, @PathVariable String holdId) {
        purchaseService.releaseHold(userId, holdId);
        return ResponseEntity.ok(OperationResult.success("Seat hold released successfully"));
    }

    @PostMapping("/pay/{holdId}")
    public ResponseEntity<PurchaseService.CompletedPurchaseView> completeMockPayment(@AuthenticationPrincipal long userId, @PathVariable String holdId) {
        return ResponseEntity.ok(purchaseService.completeMockPayment(userId, holdId));
    }

    public record CreateHoldPayload(long eventId, List<HoldItemPayload> items) {}

    public record HoldItemPayload(long seatId, long ticketClassId) {}

}
