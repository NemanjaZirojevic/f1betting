package com.sportygroup.f1betting.service;

import com.sportygroup.f1betting.entity.Event;
import com.sportygroup.f1betting.model.EventDetails;
import com.sportygroup.f1betting.repository.EventRepository;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EventService {

  private final EventProvider eventProvider;
  private final EventRepository eventRepository;

  public List<EventDetails> findEvents(String sessionType, String year, String country) {
    return eventProvider.getAllEvents(sessionType,year,country);
  }

  public Optional<Event> findEventById(Long eventId) {
    return eventRepository.findById(eventId);
  }

  public Event saveEvent(Event event) {
    return eventRepository.save(event);
  }

}
