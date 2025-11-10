package com.sportygroup.f1betting.service;

import com.sportygroup.f1betting.entity.Event;
import com.sportygroup.f1betting.model.DriverMarket;
import com.sportygroup.f1betting.model.EventDetails;
import com.sportygroup.f1betting.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

  @Mock private EventProvider eventProvider;
  @Mock private EventRepository eventRepository;

  @InjectMocks
  private EventService eventService;

  @Test
  void findEvents_delegatesToProvider_andReturnsValue() {
    // given
    String sessionType = "Race";
    String year = "2024";
    String country = "Austria";

    var details = List.of(
        new EventDetails("sk1", "Race", "2024", "Austria",
            List.of(new DriverMarket("44", "Lewis Hamilton", 3))),
        new EventDetails("sk2", "Race", "2024", "Austria",
            List.of(new DriverMarket("1", "Max Verstappen", 2)))
    );

    when(eventProvider.getAllEvents(sessionType, year, country)).thenReturn(details);

    // when
    List<EventDetails> result = eventService.findEvents(sessionType, year, country);

    // then
    verify(eventProvider).getAllEvents(sessionType, year, country);
    verifyNoInteractions(eventRepository); // ensure repo isn’t touched by findEvents
    assertThat(result).isEqualTo(details);
  }

  @Test
  void findEventById_returnsEventWhenFound() {
    // given
    Long eventId = 123L;
    Event e = Event.builder()
        .id(eventId)
        .winnerDriverId(99L)
        .settledAt(Instant.now())
        .build();

    when(eventRepository.findById(eventId)).thenReturn(Optional.of(e));

    // when
    Optional<Event> found = eventService.findEventById(eventId);

    // then
    verify(eventRepository).findById(eventId);
    assertThat(found).contains(e);
  }

  @Test
  void findEventById_returnsEmptyWhenMissing() {
    // given
    Long eventId = 777L;
    when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

    // when
    Optional<Event> found = eventService.findEventById(eventId);

    // then
    verify(eventRepository).findById(eventId);
    assertThat(found).isEmpty();
  }

  @Test
  void saveEvent_persistsAndReturnsSavedEntity() {
    // given
    Event toSave = Event.builder().id(42L).build();
    Event saved  = toSave.toBuilder().winnerDriverId(30L).build();

    when(eventRepository.save(toSave)).thenReturn(saved);

    // when
    Event result = eventService.saveEvent(toSave);

    // then
    verify(eventRepository).save(toSave);
    assertThat(result).isEqualTo(saved);
  }
}
