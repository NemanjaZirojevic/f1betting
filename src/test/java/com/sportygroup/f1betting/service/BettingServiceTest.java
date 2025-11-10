package com.sportygroup.f1betting.service;

import com.sportygroup.f1betting.entity.Bet;
import com.sportygroup.f1betting.entity.BetStatus;
import com.sportygroup.f1betting.entity.Event;
import com.sportygroup.f1betting.entity.User;
import com.sportygroup.f1betting.exceptions.DuplicateBetException;
import com.sportygroup.f1betting.model.EventOutcome;
import com.sportygroup.f1betting.model.PlaceBetRequest;
import com.sportygroup.f1betting.repository.BetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BettingServiceTest {

  @Mock private BetRepository betRepository;
  @Mock private UserService userService;
  @Mock private EventService eventService;

  @InjectMocks
  private BettingService bettingService;

  private final Long USER_ID = 10L;
  private final Long EVENT_ID = 20L;
  private final Long DRIVER_WINNER = 30L;
  private final Long DRIVER_LOSER = 40L;

  private PlaceBetRequest placeBetRequest;

  @BeforeEach
  void setUp() {
    placeBetRequest = new PlaceBetRequest(
        USER_ID, EVENT_ID, DRIVER_WINNER, 2.5, 100.0
    );
  }

  @Test
  void placeBet_createsEventIfMissing_andSavesBet() {
    // given
    when(betRepository.existsByUser_IdAndEvent_Id(USER_ID, EVENT_ID)).thenReturn(false);
    when(eventService.findEventById(EVENT_ID)).thenReturn(Optional.empty());
    Event newEvent = Event.builder().id(EVENT_ID).build();
    when(eventService.saveEvent(any(Event.class))).thenReturn(newEvent);

    User user = User.builder().id(USER_ID).balance(500.0).build();
    when(userService.checkUserBalance(placeBetRequest)).thenReturn(user);


    ArgumentCaptor<Bet> betCaptor = ArgumentCaptor.forClass(Bet.class);
    when(betRepository.save(betCaptor.capture())).thenAnswer(inv -> {
      Bet b = betCaptor.getValue();
      return b.toBuilder().build();
    });

    // when
    Bet saved = bettingService.placeBet(placeBetRequest);

    // then
    verify(betRepository).existsByUser_IdAndEvent_Id(USER_ID, EVENT_ID);
    verify(eventService).findEventById(EVENT_ID);
    verify(eventService).saveEvent(argThat(e ->
        e.getId().equals(EVENT_ID) && e.getWinnerDriverId() == null && e.getSettledAt() == null));

    verify(userService).checkUserBalance(placeBetRequest);

    Bet toSave = betCaptor.getValue();
    assertThat(toSave.getUser().getId()).isEqualTo(USER_ID);
    assertThat(toSave.getEvent().getId()).isEqualTo(EVENT_ID);
    assertThat(toSave.getDriverId()).isEqualTo(DRIVER_WINNER);
    assertThat(toSave.getAmount()).isEqualTo(100.0);
    assertThat(toSave.getOdds()).isEqualTo(2.5);
    assertThat(toSave.getStatus()).isEqualTo(BetStatus.PENDING);

    assertThat(saved).isNotNull();
  }

  @Test
  void placeBet_throws_whenDuplicateBetExists() {
    // given
    when(betRepository.existsByUser_IdAndEvent_Id(USER_ID, EVENT_ID)).thenReturn(true);

    // when / then
    assertThatThrownBy(() -> bettingService.placeBet(placeBetRequest))
        .isInstanceOf(DuplicateBetException.class)
        .hasMessageContaining("already placed a bet");

    verifyNoInteractions(eventService);
    verify(userService, never()).checkUserBalance(any());
    verify(betRepository, never()).save(any());
  }

  @Test
  void settleOutcome_updatesStatuses_balances_marksEvent_andReturnsOutcome() {
    // given: two pending bets, one wins, one loses
    User u1 = User.builder().id(1L).balance(1000.0).build();
    User u2 = User.builder().id(2L).balance(500.0).build();

    Bet b1 = Bet.builder()
        .id(101L).user(u1)
        .event(Event.builder().id(EVENT_ID).build())
        .driverId(DRIVER_WINNER).amount(100.0).odds(3.0)
        .status(BetStatus.PENDING).createdAt(Instant.now())
        .build();

    Bet b2 = Bet.builder()
        .id(102L).user(u2)
        .event(Event.builder().id(EVENT_ID).build())
        .driverId(DRIVER_LOSER).amount(50.0).odds(2.0)
        .status(BetStatus.PENDING).createdAt(Instant.now())
        .build();

    when(betRepository.findByEvent_IdAndStatus(EVENT_ID, BetStatus.PENDING))
        .thenReturn(List.of(b1, b2));

    when(eventService.findEventById(EVENT_ID))
        .thenReturn(Optional.of(Event.builder().id(EVENT_ID).build()));
    when(eventService.saveEvent(any(Event.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    when(userService.findAllById(Set.of(1L, 2L))).thenReturn(List.of(u1, u2));
    ArgumentCaptor<List<User>> usersCaptor = ArgumentCaptor.forClass(List.class);
    doNothing().when(userService).saveAll(usersCaptor.capture());

    // bet statuses updated
    ArgumentCaptor<List<Bet>> betsCaptor = ArgumentCaptor.forClass(List.class);
    when(betRepository.saveAll(betsCaptor.capture())).thenReturn(List.of());

    // outcome counts
    when(betRepository.countByEvent_IdAndStatus(EVENT_ID, BetStatus.WON)).thenReturn(1L);
    when(betRepository.countByEvent_IdAndStatus(EVENT_ID, BetStatus.LOST)).thenReturn(1L);

    // when
    EventOutcome outcome = bettingService.settleOutcome(EVENT_ID, DRIVER_WINNER);

    // then
    verify(eventService).saveEvent(argThat(e ->
        e.getId().equals(EVENT_ID)
            && e.getWinnerDriverId().equals(DRIVER_WINNER)
            && e.getSettledAt() != null));


    @SuppressWarnings("unchecked")
    List<Bet> savedBets = betsCaptor.getValue();
    assertThat(savedBets).hasSize(2);
    assertThat(savedBets)
        .anyMatch(b -> b.getId().equals(101L) && b.getStatus() == BetStatus.WON)
        .anyMatch(b -> b.getId().equals(102L) && b.getStatus() == BetStatus.LOST);


    @SuppressWarnings("unchecked")
    List<User> savedUsers = usersCaptor.getValue();
    assertThat(savedUsers).hasSize(2);
    assertThat(savedUsers)
        .anyMatch(u -> u.getId().equals(1L) && u.getBalance() == 1300.0)
        .anyMatch(u -> u.getId().equals(2L) && u.getBalance() == 450.0);


    assertThat(outcome.eventId()).isEqualTo(EVENT_ID);
    assertThat(outcome.winnerId()).isEqualTo(DRIVER_WINNER);
    assertThat(outcome.numberOfWinningBets()).isEqualTo(1L);
    assertThat(outcome.numberOfLostBets()).isEqualTo(1L);
  }

  @Test
  void settleOutcome_noPendingBets_doesNothingButCounts() {
    when(betRepository.findByEvent_IdAndStatus(EVENT_ID, BetStatus.PENDING))
        .thenReturn(List.of());

    when(eventService.findEventById(EVENT_ID)).thenReturn(Optional.empty());
    when(eventService.saveEvent(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

    when(betRepository.countByEvent_IdAndStatus(EVENT_ID, BetStatus.WON)).thenReturn(0L);
    when(betRepository.countByEvent_IdAndStatus(EVENT_ID, BetStatus.LOST)).thenReturn(0L);

    EventOutcome outcome = bettingService.settleOutcome(EVENT_ID, DRIVER_WINNER);

    verify(betRepository, never()).saveAll(anyList());
    verify(userService, never()).saveAll(anyList());
    assertThat(outcome.numberOfWinningBets()).isZero();
    assertThat(outcome.numberOfLostBets()).isZero();
  }
}
