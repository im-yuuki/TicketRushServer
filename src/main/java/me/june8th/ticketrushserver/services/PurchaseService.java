package me.june8th.ticketrushserver.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.june8th.ticketrushserver.data.Event;
import me.june8th.ticketrushserver.data.Purchase;
import me.june8th.ticketrushserver.data.SalesRound;
import me.june8th.ticketrushserver.data.Seat;
import me.june8th.ticketrushserver.data.Ticket;
import me.june8th.ticketrushserver.data.TicketClass;
import me.june8th.ticketrushserver.data.UserAccount;
import me.june8th.ticketrushserver.database.PurchaseRepository;
import me.june8th.ticketrushserver.database.SalesRoundRepository;
import me.june8th.ticketrushserver.database.SeatRepository;
import me.june8th.ticketrushserver.database.TicketClassRepository;
import me.june8th.ticketrushserver.database.TicketRepository;
import me.june8th.ticketrushserver.database.UserRepository;
import me.june8th.ticketrushserver.types.ForbiddenException;
import me.june8th.ticketrushserver.types.InvalidStateException;
import me.june8th.ticketrushserver.types.ResourceConflictException;
import me.june8th.ticketrushserver.types.ResourceNotFoundException;
import me.june8th.ticketrushserver.types.TimedOutException;
import me.june8th.ticketrushserver.types.PurchaseData.ActiveHoldView;
import me.june8th.ticketrushserver.types.PurchaseData.CompletedPurchaseView;
import me.june8th.ticketrushserver.types.PurchaseData.HeldItemView;
import me.june8th.ticketrushserver.types.PurchaseData.HoldCart;
import me.june8th.ticketrushserver.types.PurchaseData.HoldCartItem;
import me.june8th.ticketrushserver.types.PurchaseData.HoldCartSnapshot;
import me.june8th.ticketrushserver.types.PurchaseData.HoldItemRequest;
import me.june8th.ticketrushserver.types.PurchaseData.HoldView;
import me.june8th.ticketrushserver.types.PurchaseData.MockPaymentDetails;
import me.june8th.ticketrushserver.types.PurchaseData.PurchaseEventView;
import me.june8th.ticketrushserver.types.PurchaseData.SalesRoundView;
import me.june8th.ticketrushserver.types.SeatAvailability;
import me.june8th.ticketrushserver.types.PurchaseData.SeatRowView;
import me.june8th.ticketrushserver.types.PurchaseData.SeatStatusCollectionView;
import me.june8th.ticketrushserver.types.PurchaseData.SeatView;
import me.june8th.ticketrushserver.types.PurchaseData.SeatZoneView;
import me.june8th.ticketrushserver.types.PurchaseData.TicketClassView;
import me.june8th.ticketrushserver.types.PurchaseData.UserTicketView;
import me.june8th.ticketrushserver.utils.RandomGenerator;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseService {

    private static final long HOLD_TTL_SECONDS = 900L;
    private static final String HOLD_SEAT_KEY_PREFIX = "ticketrush:hold:seat:";
    private static final String HOLD_CART_KEY_PREFIX = "ticketrush:hold:cart:";
    private static final String HOLD_USER_KEY_PREFIX = "ticketrush:hold:user:";

    private static final DefaultRedisScript<Long> CREATE_HOLD_SCRIPT = createScript("""
            local holdId = ARGV[1]
            local ttl = tonumber(ARGV[2])
            local cartJson = ARGV[3]

            if redis.call('EXISTS', KEYS[1]) == 1 then
                return -1
            end

            redis.call('SET', KEYS[1], holdId, 'EX', ttl)
            redis.call('SET', KEYS[2], cartJson, 'EX', ttl)
            return 1
            """);

    private static final DefaultRedisScript<Long> ADD_SEAT_TO_HOLD_SCRIPT = createScript("""
            local holdId = ARGV[1]
            local ttl = tonumber(ARGV[2])
            local currentCartJson = ARGV[3]
            local nextCartJson = ARGV[4]

            if redis.call('GET', KEYS[1]) ~= holdId then
                return -1
            end

            if redis.call('GET', KEYS[2]) ~= currentCartJson then
                return -2
            end

            if redis.call('EXISTS', KEYS[3]) == 1 then
                return 0
            end

            redis.call('SET', KEYS[1], holdId, 'EX', ttl)
            redis.call('SET', KEYS[2], nextCartJson, 'EX', ttl)
            redis.call('SET', KEYS[3], holdId, 'EX', ttl)
            return 1
            """);

    private static final DefaultRedisScript<Long> RELEASE_HOLD_SCRIPT = createScript("""
            local holdId = ARGV[1]
            local seatCount = tonumber(ARGV[2])
            local userKeyIndex = seatCount + 1
            local cartKeyIndex = seatCount + 2

            for i = 1, seatCount do
                if redis.call('GET', KEYS[i]) == holdId then
                    redis.call('DEL', KEYS[i])
                end
            end

            if redis.call('GET', KEYS[userKeyIndex]) == holdId then
                redis.call('DEL', KEYS[userKeyIndex])
            end

            redis.call('DEL', KEYS[cartKeyIndex])
            return 1
            """);

    private final EventService eventService;
    private final UserRepository userRepository;
    private final SeatRepository seatRepository;
    private final SalesRoundRepository salesRoundRepository;
    private final TicketClassRepository ticketClassRepository;
    private final TicketRepository ticketRepository;
    private final PurchaseRepository purchaseRepository;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Transactional(readOnly = true)
    public PurchaseEventView getPurchaseEvent(long userId, long eventId) {
        Instant now = Instant.now();
        Event event = eventService.getPublishedEvent(eventId);
        ensurePurchasable(event, now);

        List<SalesRound> salesRounds = salesRoundRepository.findAllByEvent_IdOrderByStartTimeAscIdAsc(eventId);
        List<TicketClass> ticketClasses = ticketClassRepository.findAllBySalesRound_Event_IdOrderBySalesRoundStartTimeAscIdAsc(eventId);
        List<Seat> seats = seatRepository.findAllByEventId(eventId);
        HoldCart myHold = findUserEventHold(userId, eventId);

        Set<Long> activeRoundIds = new HashSet<>();
        List<SalesRoundView> activeSalesRounds = new ArrayList<>();
        for (SalesRound salesRound : salesRounds) {
            if (!isSalesRoundActive(salesRound, now)) continue;
            activeRoundIds.add(salesRound.getId());
            activeSalesRounds.add(new SalesRoundView(
                    salesRound.getId(),
                    salesRound.getName(),
                    salesRound.getStartTime(),
                    salesRound.getEndTime(),
                    salesRound.getMaxTicketsPerPurchase()
            ));
        }

        List<TicketClassView> activeTicketClasses = ticketClasses.stream()
                .filter(ticketClass -> activeRoundIds.contains(ticketClass.getSalesRound().getId()))
                .map(ticketClass -> new TicketClassView(
                        ticketClass.getId(),
                        ticketClass.getName(),
                        ticketClass.getDescription(),
                        ticketClass.getPrice(),
                        ticketClass.getSalesRound().getId(),
                        ticketClass.getSeatZone().getId()
                ))
                .toList();

        SeatStatusCollectionView seatStatusCollection = buildSeatStatusCollection(myHold, seats, eventId);
        return new PurchaseEventView(
                event.getId(),
                event.getName(),
                event.getDateTime(),
                activeSalesRounds,
                activeTicketClasses,
                seatStatusCollection.seatZones(),
                seatStatusCollection.myActiveHold()
        );
    }

    @Transactional(readOnly = true)
    public SeatStatusCollectionView getSeatStatuses(long userId, long eventId) {
        Instant now = Instant.now();
        Event event = eventService.getPublishedEvent(eventId);
        ensurePurchasable(event, now);
        HoldCart myHold = findUserEventHold(userId, eventId);
        return buildSeatStatusCollection(myHold, seatRepository.findAllByEventId(eventId), eventId);
    }

    public HoldView createHold(long userId, long eventId) {
        getUser(userId);

        Instant now = Instant.now();
        Event event = eventService.getPublishedEvent(eventId);
        ensurePurchasable(event, now);

        String holdId = RandomGenerator.generateRequestKey();
        Instant expiresAt = now.plusSeconds(HOLD_TTL_SECONDS);
        HoldCart holdCart = new HoldCart(holdId, userId, eventId, expiresAt, 0L, List.of());

        Long result = stringRedisTemplate.execute(
                CREATE_HOLD_SCRIPT,
                List.of(userEventKey(userId, eventId), cartKey(holdId)),
                holdId,
                String.valueOf(HOLD_TTL_SECONDS),
                serializeHoldCart(holdCart)
        );

        if (Objects.equals(result, -1L)) {
            HoldCart existingHold = findUserEventHold(userId, eventId);
            if (existingHold == null) throw new RuntimeException("You already have an active hold for this event, but it could not be retrieved");
            log.debug("Reusing existing hold {} for user {} on event {}", existingHold.holdId(), userId, eventId);
            return toHoldView(existingHold);
        }
        if (!Objects.equals(result, 1L)) {
            throw new InvalidStateException("Failed to create seat hold");
        }

        log.debug("Created empty hold {} for user {} on event {}", holdId, userId, eventId);
        return toHoldView(holdCart);
    }

    public HoldView addSeatToHold(long userId, String holdId, HoldItemRequest item) {
        validateHoldItemIds(List.of(item));
        HoldCartSnapshot snapshot = getOwnedHoldSnapshot(userId, holdId);
        HoldCart holdCart = snapshot.holdCart();
        Instant now = Instant.now();

        Event event = eventService.getPublishedEvent(holdCart.eventId());
        ensurePurchasable(event, now);
        ensureSeatNotAlreadyInHold(holdCart, item.seatId());

        List<HoldItemRequest> nextItems = new ArrayList<>(holdCart.items().size() + 1);
        holdCart.items().forEach(existingItem -> nextItems.add(new HoldItemRequest(existingItem.seatId(), existingItem.ticketClassId())));
        nextItems.add(item);

        List<Long> seatIds = nextItems.stream().map(HoldItemRequest::seatId).toList();
        ensureUniqueSeatSelection(seatIds);

        List<Seat> seats = seatRepository.findAllById(seatIds);
        if (seats.size() != seatIds.size()) {
            throw new ResourceNotFoundException("One or more seats were not found");
        }
        Map<Long, Seat> seatMap = seatsById(seats);

        List<Long> ticketClassIds = nextItems.stream().map(HoldItemRequest::ticketClassId).distinct().toList();
        List<TicketClass> ticketClasses = ticketClassRepository.findAllById(ticketClassIds);
        if (ticketClasses.size() != ticketClassIds.size()) {
            throw new ResourceNotFoundException("One or more ticket classes were not found");
        }
        Map<Long, TicketClass> ticketClassMap = ticketClassesById(ticketClasses);

        validatePurchaseSelection(holdCart.eventId(), nextItems, seatMap, ticketClassMap, now);

        for (Seat seat : seats) {
            if (seat.getAssociatedTicket() != null) {
                throw new ResourceConflictException("One or more seats are already sold");
            }
        }

        HoldCart nextHoldCart = new HoldCart(
                holdCart.holdId(),
                holdCart.userId(),
                holdCart.eventId(),
                holdCart.expiresAt(),
                calculateTotalAmount(nextItems, ticketClassMap),
                nextItems.stream().map(nextItem -> new HoldCartItem(nextItem.seatId(), nextItem.ticketClassId(), ticketClassMap.get(nextItem.ticketClassId()).getPrice())).toList()
        );

        long ttlSeconds = getRemainingHoldTtlSeconds(holdCart, now);
        Long result = stringRedisTemplate.execute(
                ADD_SEAT_TO_HOLD_SCRIPT,
                List.of(userEventKey(userId, holdCart.eventId()), cartKey(holdId), seatKey(item.seatId())),
                holdId,
                String.valueOf(ttlSeconds),
                snapshot.rawJson(),
                serializeHoldCart(nextHoldCart)
        );

        if (Objects.equals(result, -1L)) {
            throw new TimedOutException("This seat hold has expired");
        }
        if (Objects.equals(result, -2L)) {
            throw new InvalidStateException("Seat hold changed, please refresh and try again");
        }
        if (Objects.equals(result, 0L)) {
            throw new ResourceConflictException("The selected seat is no longer available");
        }
        if (!Objects.equals(result, 1L)) {
            throw new InvalidStateException("Failed to update seat hold");
        }

        log.debug("Added seat {} to hold {} for user {}", item.seatId(), holdId, userId);
        return toHoldView(nextHoldCart);
    }

    public HoldView getHold(long userId, String holdId) {
        return toHoldView(getOwnedHold(userId, holdId));
    }

    public void releaseHold(long userId, String holdId) {
        HoldCart holdCart = getOwnedHold(userId, holdId);
        releaseHoldKeys(holdCart);
        log.debug("Released hold {} for user {}", holdId, userId);
    }

    @Transactional
    public CompletedPurchaseView completeMockPayment(long userId, String holdId) {
        HoldCart holdCart = getOwnedHold(userId, holdId);
        if (holdCart.items().isEmpty()) {
            throw new InvalidStateException("You must hold at least one seat before paying");
        }
        UserAccount user = getUser(userId);
        Instant now = Instant.now();

        Event event = eventService.getPublishedEvent(holdCart.eventId());
        ensurePurchasable(event, now);

        List<Long> seatIds = holdCart.items().stream().map(HoldCartItem::seatId).toList();
        List<Seat> seats = seatRepository.findAllByIdInForUpdate(seatIds);
        if (seats.size() != seatIds.size()) {
            throw new InvalidStateException("One or more held seats no longer exist");
        }
        Map<Long, Seat> seatMap = seatsById(seats);

        List<Long> ticketClassIds = holdCart.items().stream().map(HoldCartItem::ticketClassId).distinct().toList();
        List<TicketClass> ticketClasses = ticketClassRepository.findAllById(ticketClassIds);
        if (ticketClasses.size() != ticketClassIds.size()) {
            throw new InvalidStateException("One or more held ticket classes no longer exist");
        }
        Map<Long, TicketClass> ticketClassMap = ticketClassesById(ticketClasses);

        validatePurchaseSelection(
                holdCart.eventId(),
                holdCart.items().stream().map(item -> new HoldItemRequest(item.seatId(), item.ticketClassId())).toList(),
                seatMap,
                ticketClassMap,
                now
        );

        for (HoldCartItem item : holdCart.items()) {
            Seat seat = seatMap.get(item.seatId());
            TicketClass ticketClass = ticketClassMap.get(item.ticketClassId());
            if (seat.getAssociatedTicket() != null) {
                throw new ResourceConflictException("One or more seats are already sold");
            }
            if (ticketClass.getPrice() != item.price()) {
                throw new InvalidStateException("Ticket pricing changed while the hold was active");
            }
        }

        Purchase purchase = purchaseRepository.save(Purchase.builder()
                .user(user)
                .amount(holdCart.totalAmount())
                .details(serializeMockPaymentDetails(holdCart, now))
                .build());

        List<Ticket> savedTickets = new ArrayList<>(holdCart.items().size());
        for (HoldCartItem item : holdCart.items()) {
            Seat seat = seatMap.get(item.seatId());
            TicketClass ticketClass = ticketClassMap.get(item.ticketClassId());
            Ticket ticket = ticketRepository.save(Ticket.builder()
                    .ticketClass(ticketClass)
                    .seat(seat)
                    .user(user)
                    .purchase(purchase)
                    .build());
            seat.setAssociatedTicket(ticket);
            savedTickets.add(ticket);
        }
        seatRepository.saveAll(seats);

        scheduleHoldRelease(holdCart);
        log.debug("Completed mock payment for hold {} as purchase {}", holdId, purchase.getId());
        return new CompletedPurchaseView(
                purchase.getId(),
                purchase.getAmount(),
                savedTickets.stream().map(Ticket::getId).toList()
        );
    }

    @Transactional(readOnly = true)
    public List<UserTicketView> getUserTickets(long userId) {
        UserAccount user = getUser(userId);
        return ticketRepository.findAllByUserOrderByCreatedAtDesc(user).stream()
                .map(ticket -> new UserTicketView(
                        ticket.getId(),
                        ticket.getPurchase().getAt(),
                        ticket.getPurchase().getId(),
                        ticket.getTicketClass().getSalesRound().getEvent().getId(),
                        ticket.getTicketClass().getSalesRound().getEvent().getName(),
                        ticket.getTicketClass().getSalesRound().getEvent().getDateTime(),
                        ticket.getTicketClass().getSalesRound().getName(),
                        ticket.getTicketClass().getName(),
                        ticket.getSeat().getSeatRow().getSeatZone().getName(),
                        ticket.getSeat().getSeatRow().getLabel(),
                        ticket.getSeat().getNumber(),
                        ticket.getTicketSecretCode(),
                        ticket.getCheckedInAt()
                ))
                .toList();
    }

    private static DefaultRedisScript<Long> createScript(String scriptText) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(scriptText);
        script.setResultType(Long.class);
        return script;
    }

    private void ensurePurchasable(Event event, Instant now) {
        if (!event.getDateTime().isAfter(now)) {
            throw new InvalidStateException("This event is no longer available for purchase");
        }
    }

    private void validateHoldItemIds(Collection<HoldItemRequest> items) {
        for (HoldItemRequest item : items) {
            if (item.seatId() <= 0 || item.ticketClassId() <= 0) {
                throw new IllegalArgumentException("Seat and ticket class ids must be positive");
            }
        }
    }

    private void ensureUniqueSeatSelection(List<Long> seatIds) {
        if (new HashSet<>(seatIds).size() != seatIds.size()) {
            throw new InvalidStateException("Seat selection contains duplicates");
        }
    }

    private void ensureSeatNotAlreadyInHold(HoldCart holdCart, long seatId) {
        boolean seatAlreadyHeld = holdCart.items().stream().anyMatch(item -> item.seatId() == seatId);
        if (seatAlreadyHeld) {
            throw new InvalidStateException("This seat is already in your hold");
        }
    }

    private void validatePurchaseSelection(
            long eventId,
            List<HoldItemRequest> items,
            Map<Long, Seat> seatMap,
            Map<Long, TicketClass> ticketClassMap,
            Instant now
    ) {
        Map<Long, Integer> salesRoundCounts = new HashMap<>();
        for (HoldItemRequest item : items) {
            Seat seat = seatMap.get(item.seatId());
            TicketClass ticketClass = ticketClassMap.get(item.ticketClassId());
            if (seat.getSeatRow().getSeatZone().getEvent().getId() != eventId) {
                throw new InvalidStateException("Selected seat does not belong to this event");
            }
            if (ticketClass.getSalesRound().getEvent().getId() != eventId) {
                throw new InvalidStateException("Selected ticket class does not belong to this event");
            }
            if (ticketClass.getSeatZone().getId() != seat.getSeatRow().getSeatZone().getId()) {
                throw new InvalidStateException("Selected seat does not belong to the ticket class zone");
            }
            if (!isSalesRoundActive(ticketClass.getSalesRound(), now)) {
                throw new InvalidStateException("One or more selected ticket classes are not currently on sale");
            }
            salesRoundCounts.merge(ticketClass.getSalesRound().getId(), 1, Integer::sum);
        }
        validateSalesRoundLimits(salesRoundCounts, ticketClassMap);
    }

    private void validateSalesRoundLimits(Map<Long, Integer> salesRoundCounts, Map<Long, TicketClass> ticketClassMap) {
        Map<Long, SalesRound> salesRounds = new HashMap<>();
        for (TicketClass ticketClass : ticketClassMap.values()) {
            salesRounds.put(ticketClass.getSalesRound().getId(), ticketClass.getSalesRound());
        }
        for (Map.Entry<Long, Integer> entry : salesRoundCounts.entrySet()) {
            SalesRound salesRound = salesRounds.get(entry.getKey());
            int maxTicketsPerPurchase = salesRound.getMaxTicketsPerPurchase();
            if (maxTicketsPerPurchase > 0 && entry.getValue() > maxTicketsPerPurchase) {
                throw new InvalidStateException("Purchase exceeds the maximum tickets allowed for sales round " + salesRound.getName());
            }
        }
    }

    private boolean isSalesRoundActive(SalesRound salesRound, Instant now) {
        return !salesRound.getStartTime().isAfter(now) && salesRound.getEndTime().isAfter(now);
    }

    private Map<Long, Seat> seatsById(List<Seat> seats) {
        Map<Long, Seat> seatMap = new HashMap<>();
        for (Seat seat : seats) {
            seatMap.put(seat.getId(), seat);
        }
        return seatMap;
    }

    private Map<Long, TicketClass> ticketClassesById(List<TicketClass> ticketClasses) {
        Map<Long, TicketClass> ticketClassMap = new HashMap<>();
        for (TicketClass ticketClass : ticketClasses) {
            ticketClassMap.put(ticketClass.getId(), ticketClass);
        }
        return ticketClassMap;
    }

    private UserAccount getUser(long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private long calculateTotalAmount(List<HoldItemRequest> items, Map<Long, TicketClass> ticketClassMap) {
        long totalAmount = 0L;
        for (HoldItemRequest item : items) {
            totalAmount = Math.addExact(totalAmount, ticketClassMap.get(item.ticketClassId()).getPrice());
        }
        return totalAmount;
    }

    private long getRemainingHoldTtlSeconds(HoldCart holdCart, Instant now) {
        if (!holdCart.expiresAt().isAfter(now)) {
            throw new TimedOutException("This seat hold has expired");
        }
        return Math.max(1L, holdCart.expiresAt().getEpochSecond() - now.getEpochSecond());
    }

    private HoldCart getOwnedHold(long userId, String holdId) {
        HoldCart holdCart = getHoldById(holdId);
        if (holdCart.userId() != userId) {
            throw new ForbiddenException("This seat hold does not belong to you");
        }
        if (!holdCart.expiresAt().isAfter(Instant.now())) {
            throw new TimedOutException("This seat hold has expired");
        }
        return holdCart;
    }

    private HoldCart getHoldById(String holdId) {
        return getHoldSnapshotById(holdId).holdCart();
    }

    private HoldCartSnapshot getOwnedHoldSnapshot(long userId, String holdId) {
        HoldCartSnapshot snapshot = getHoldSnapshotById(holdId);
        HoldCart holdCart = snapshot.holdCart();
        if (holdCart.userId() != userId) {
            throw new ForbiddenException("This seat hold does not belong to you");
        }
        if (!holdCart.expiresAt().isAfter(Instant.now())) {
            throw new TimedOutException("This seat hold has expired");
        }
        return snapshot;
    }

    private HoldCartSnapshot getHoldSnapshotById(String holdId) {
        String cartJson = stringRedisTemplate.opsForValue().get(cartKey(holdId));
        if (cartJson == null) {
            throw new TimedOutException("This seat hold has expired");
        }
        try {
            return new HoldCartSnapshot(cartJson, objectMapper.readValue(cartJson, HoldCart.class));
        }
        catch (JsonProcessingException exception) {
            log.error("Failed to parse hold cart {}", holdId, exception);
            throw new InvalidStateException("Seat hold data is invalid");
        }
    }

    private HoldCart findUserEventHold(long userId, long eventId) {
        String holdId = stringRedisTemplate.opsForValue().get(userEventKey(userId, eventId));
        if (holdId == null) {
            return null;
        }
        String cartJson = stringRedisTemplate.opsForValue().get(cartKey(holdId));
        if (cartJson == null) {
            return null;
        }
        try {
            return objectMapper.readValue(cartJson, HoldCart.class);
        }
        catch (JsonProcessingException exception) {
            log.error("Failed to parse hold cart {}", holdId, exception);
            return null;
        }
    }

    private HoldView toHoldView(HoldCart holdCart) {
        return new HoldView(
                holdCart.holdId(),
                holdCart.expiresAt(),
                holdCart.totalAmount(),
                holdCart.items().stream()
                        .map(item -> new HeldItemView(item.seatId(), item.ticketClassId(), item.price()))
                        .toList()
        );
    }

    private String serializeHoldCart(HoldCart holdCart) {
        try {
            return objectMapper.writeValueAsString(holdCart);
        }
        catch (JsonProcessingException exception) {
            throw new InvalidStateException("Failed to create seat hold");
        }
    }

    private String serializeMockPaymentDetails(HoldCart holdCart, Instant paidAt) {
        try {
            return objectMapper.writeValueAsString(new MockPaymentDetails("mock", holdCart.holdId(), paidAt, holdCart.totalAmount(), holdCart.items()));
        }
        catch (JsonProcessingException exception) {
            throw new InvalidStateException("Failed to finalize purchase");
        }
    }

    private void scheduleHoldRelease(HoldCart holdCart) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            releaseHoldKeys(holdCart);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                releaseHoldKeys(holdCart);
            }
        });
    }

    private void releaseHoldKeys(HoldCart holdCart) {
        List<String> keys = new ArrayList<>(holdCart.items().size() + 2);
        holdCart.items().forEach(item -> keys.add(seatKey(item.seatId())));
        keys.add(userEventKey(holdCart.userId(), holdCart.eventId()));
        keys.add(cartKey(holdCart.holdId()));
        stringRedisTemplate.execute(
                RELEASE_HOLD_SCRIPT,
                keys,
                holdCart.holdId(),
                String.valueOf(holdCart.items().size())
        );
    }

    private String seatKey(long seatId) {
        return HOLD_SEAT_KEY_PREFIX + seatId;
    }

    private String cartKey(String holdId) {
        return HOLD_CART_KEY_PREFIX + holdId;
    }

    private String userEventKey(long userId, long eventId) {
        return HOLD_USER_KEY_PREFIX + userId + ":" + eventId;
    }

    private SeatStatusCollectionView buildSeatStatusCollection(HoldCart myHold, List<Seat> seats, long eventId) {
        List<String> seatKeys = seats.stream().map(seat -> seatKey(seat.getId())).toList();
        List<String> seatHoldValues = seats.isEmpty() ? List.of() : stringRedisTemplate.opsForValue().multiGet(seatKeys);
        String myHoldId = myHold == null ? null : myHold.holdId();

        Map<Long, SeatAvailability> seatAvailability = new HashMap<>();
        for (int i = 0; i < seats.size(); i++) {
            Seat seat = seats.get(i);
            String holdValue = seatHoldValues != null && i < seatHoldValues.size() ? seatHoldValues.get(i) : null;
            SeatAvailability availability = SeatAvailability.AVAILABLE;
            if (seat.getAssociatedTicket() != null) {
                availability = SeatAvailability.SOLD;
            }
            else if (holdValue != null && holdValue.equals(myHoldId)) {
                availability = SeatAvailability.HELD_BY_ME;
            }
            else if (holdValue != null) {
                availability = SeatAvailability.HELD;
            }
            seatAvailability.put(seat.getId(), availability);
        }

        Map<Long, SeatZoneBuilder> zoneBuilders = new LinkedHashMap<>();
        for (Seat seat : seats) {
            var seatRow = seat.getSeatRow();
            var seatZone = seatRow.getSeatZone();
            SeatZoneBuilder zoneBuilder = zoneBuilders.computeIfAbsent(
                    seatZone.getId(),
                    ignored -> new SeatZoneBuilder(seatZone.getId(), seatZone.getName(), seatZone.getPositionX(), seatZone.getPositionY())
            );
            zoneBuilder.addSeat(
                    seatRow.getId(),
                    seatRow.getIndex(),
                    seatRow.getLabel(),
                    new SeatView(seat.getId(), seat.getIndex(), seat.getNumber(), seatAvailability.get(seat.getId()))
            );
        }

        ActiveHoldView myActiveHold = myHold == null ? null : new ActiveHoldView(myHold.holdId(), myHold.expiresAt(), myHold.totalAmount());
        return new SeatStatusCollectionView(eventId, zoneBuilders.values().stream().map(SeatZoneBuilder::toView).toList(), myActiveHold);
    }

    private static final class SeatZoneBuilder {
        private final long id;
        private final String name;
        private final int positionX;
        private final int positionY;
        private final Map<Long, SeatRowBuilder> rows = new LinkedHashMap<>();

        private SeatZoneBuilder(long id, String name, int positionX, int positionY) {
            this.id = id;
            this.name = name;
            this.positionX = positionX;
            this.positionY = positionY;
        }

        private void addSeat(long rowId, int rowIndex, String rowLabel, SeatView seatView) {
            rows.computeIfAbsent(rowId, ignored -> new SeatRowBuilder(rowId, rowIndex, rowLabel)).seats.add(seatView);
        }

        private SeatZoneView toView() {
            return new SeatZoneView(
                    id,
                    name,
                    positionX,
                    positionY,
                    rows.values().stream().map(SeatRowBuilder::toView).toList()
            );
        }
    }

    private static final class SeatRowBuilder {
        private final long id;
        private final int index;
        private final String label;
        private final List<SeatView> seats = new ArrayList<>();

        private SeatRowBuilder(long id, int index, String label) {
            this.id = id;
            this.index = index;
            this.label = label;
        }

        private SeatRowView toView() {
            return new SeatRowView(id, index, label, seats);
        }
    }

}
