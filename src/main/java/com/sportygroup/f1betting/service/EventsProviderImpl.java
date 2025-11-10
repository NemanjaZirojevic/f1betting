package com.sportygroup.f1betting.service;

import com.sportygroup.f1betting.model.Driver;
import com.sportygroup.f1betting.model.DriverMarket;
import com.sportygroup.f1betting.model.EventDetails;
import com.sportygroup.f1betting.model.Session;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventsProviderImpl implements EventProvider{


  private final OpenF1Client openF1Client;

  @Override
  public List<EventDetails> getAllEvents(String sessionType, String year, String country) {
    var sessions = openF1Client.fetchSessions(sessionType, year, country);
    return Arrays.stream(sessions)
        .map(this::toEventDetails)
        .toList();
  }

  private EventDetails toEventDetails(Session s) {
    var drivers = openF1Client.fetchDriversBySession(s.session_key());
    var market  = Arrays.stream(drivers)
        .map(this::toDriverMarket)
        .toList();

    return new EventDetails(
        s.session_key(),
        s.session_type(),
        s.year(),
        s.country_name(),
        market
    );
  }

  private DriverMarket toDriverMarket(Driver d) {
    return new DriverMarket(String.valueOf(d.driver_number()), d.full_name(), randomOdds());
  }
  private int randomOdds() { return 2 + ThreadLocalRandom.current().nextInt(3); }

}
