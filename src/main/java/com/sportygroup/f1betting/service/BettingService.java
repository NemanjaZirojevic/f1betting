package com.sportygroup.f1betting.service;

import com.sportygroup.f1betting.entity.Bet;
import com.sportygroup.f1betting.entity.BetStatus;
import com.sportygroup.f1betting.entity.Event;
import com.sportygroup.f1betting.entity.User;
import com.sportygroup.f1betting.exceptions.DuplicateBetException;
import com.sportygroup.f1betting.exceptions.EventFinishedException;
import com.sportygroup.f1betting.model.EventOutcome;
import com.sportygroup.f1betting.model.PlaceBetRequest;
import com.sportygroup.f1betting.repository.BetRepository;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class BettingService {

  private final BetRepository betRepository;
  private final UserService userService;
  private final EventService eventService;

  @Transactional
  public Bet placeBet(PlaceBetRequest placeBetRequest) {
    validateNoDuplicateBet(placeBetRequest.userId(), placeBetRequest.eventId());

    var event = eventService.findEventById(placeBetRequest.eventId())
        .orElseGet(() -> eventService.saveEvent(Event.builder().id(placeBetRequest.eventId()).build()));

    ensureEventOpen(event,placeBetRequest.eventId());
    User user = userService.checkUserBalance(placeBetRequest);

    var bet = Bet.builder()
        .user(user)
        .event(event)
        .driverId(placeBetRequest.driverId())
        .amount(placeBetRequest.amount())
        .odds(placeBetRequest.odds())
        .status(BetStatus.PENDING)
        .build();
    return betRepository.save(bet);
  }

  @Transactional
  public EventOutcome settleOutcome(Long eventId, Long winnerDriverId) {
    List<Bet> pendingBets = betRepository.findByEvent_IdAndStatus(eventId, BetStatus.PENDING);
    markEventAsSettled(eventId, winnerDriverId);
    Map<Long, Double> deltaByUser = computeUserBalance(pendingBets, winnerDriverId);
    updateBetStatuses(pendingBets, winnerDriverId);
    updateUsersBalance(deltaByUser);
    long numberOfWinningBets = calculateNumberBets(eventId, BetStatus.WON);
    long numberOfLostBets = calculateNumberBets(eventId, BetStatus.LOST);
    return new EventOutcome(eventId, winnerDriverId, numberOfWinningBets, numberOfLostBets);
  }

  private void validateNoDuplicateBet(Long userId, Long eventId) {
    Optional.of(betRepository.existsByUser_IdAndEvent_Id(userId, eventId))
        .filter(exists -> !exists)
        .orElseThrow(() -> new DuplicateBetException(
            "User %d has already placed a bet for event %d".formatted(userId, eventId)
        ));
  }

  private void ensureEventOpen(Event event, Long eventId) {
    Optional.ofNullable(event.getWinnerDriverId())
        .ifPresent(winner -> {
          throw new EventFinishedException(
              "Can't place bet for already finished event (eventId=%d, winnerDriverId=%d)"
                  .formatted(eventId, winner));
        });
  }

  private void updateUsersBalance(Map<Long, Double> deltasByUserId) {
    if (deltasByUserId.isEmpty()) return;

    List<User> users = userService.findAllById(deltasByUserId.keySet());
    List<User> updated = users.stream()
        .map(user -> {
          double delta = deltasByUserId.getOrDefault(user.getId(), 0.0);
          return user.toBuilder().balance(user.getBalance() + delta).build();
        })
        .toList();

    userService.saveAll(updated);
  }

  private long calculateNumberBets(Long eventId, BetStatus betStatus) {
    return betRepository.countByEvent_IdAndStatus(eventId, betStatus);
  }

  private Map<Long, Double> computeUserBalance(List<Bet> pendingBets, long winnerDriverId) {
    if (pendingBets == null || pendingBets.isEmpty()) {
      return Collections.emptyMap();
    }

    return pendingBets.stream()
        .collect(Collectors.toMap(
            bet -> bet.getUser().getId(),
            bet -> {
              boolean isWinner = Objects.equals(winnerDriverId, bet.getDriverId());
              return isWinner
                  ? bet.getAmount() * bet.getOdds()
                  : -bet.getAmount();
            },
            Double::sum
        ));
  }

  private void updateBetStatuses(List<Bet> bets, long winnerDriverId) {
    if (bets == null || bets.isEmpty()) {
      return;
    }

    List<Bet> updatedBets = bets.stream()
        .map(bet -> bet.toBuilder()
            .status(Objects.equals(winnerDriverId, bet.getDriverId())
                ? BetStatus.WON
                : BetStatus.LOST)
            .build())
        .toList();

    betRepository.saveAll(updatedBets);
  }

  private Event markEventAsSettled(Long eventId, long winnerDriverId) {
    Event base = eventService.findEventById(eventId)
        .orElseGet(() -> Event.builder().id(eventId).build());

    Event settled = base.toBuilder()
        .winnerDriverId(winnerDriverId)
        .settledAt(Instant.now())
        .build();

    return eventService.saveEvent(settled);

  }

}
