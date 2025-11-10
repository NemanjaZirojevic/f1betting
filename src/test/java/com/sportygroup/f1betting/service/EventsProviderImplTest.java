package com.sportygroup.f1betting.service;

import com.sportygroup.f1betting.model.Driver;
import com.sportygroup.f1betting.model.DriverMarket;
import com.sportygroup.f1betting.model.EventDetails;
import com.sportygroup.f1betting.model.Session;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventsProviderImplTest {

  @Mock
  private OpenF1Client openF1Client;

  @InjectMocks
  private EventsProviderImpl eventsProvider;

  @Test
  void getAllEvents_mapsSessionsAndDrivers_andOddsWithinRange() {
    // given
    String sessionType = "Race";
    String year = "2024";
    String country = "Austria";

    Session s1 = session("k1", "Race", "2024", "Austria");
    Session s2 = session("k2", "Race", "2024", "Austria");

    when(openF1Client.fetchSessions(sessionType, year, country))
        .thenReturn(new Session[]{s1, s2});

    Driver[] s1Drivers = new Driver[] {
        new Driver("44", "Lewis Hamilton"),
        new Driver("1",  "Max Verstappen")
    };
    Driver[] s2Drivers = new Driver[] {
        new Driver("16", "Charles Leclerc")
    };

    when(openF1Client.fetchDriversBySession("k1")).thenReturn(s1Drivers);
    when(openF1Client.fetchDriversBySession("k2")).thenReturn(s2Drivers);

    // when
    List<EventDetails> result = eventsProvider.getAllEvents(sessionType, year, country);

    // then
    verify(openF1Client).fetchSessions(sessionType, year, country);
    verify(openF1Client).fetchDriversBySession("k1");
    verify(openF1Client).fetchDriversBySession("k2");

    assertThat(result).hasSize(2);

    EventDetails e1 = result.get(0);
    assertThat(e1.id()).isEqualTo("k1");
    assertThat(e1.sessionType()).isEqualTo("Race");
    assertThat(e1.year()).isEqualTo("2024");
    assertThat(e1.country()).isEqualTo("Austria");
    assertThat(e1.driverMarket()).hasSize(2);
    assertDriverMarket(e1.driverMarket().get(0), "44", "Lewis Hamilton");
    assertDriverMarket(e1.driverMarket().get(1), "1",  "Max Verstappen");

    EventDetails e2 = result.get(1);
    assertThat(e2.id()).isEqualTo("k2");
    assertThat(e2.sessionType()).isEqualTo("Race");
    assertThat(e2.year()).isEqualTo("2024");
    assertThat(e2.country()).isEqualTo("Austria");
    assertThat(e2.driverMarket()).hasSize(1);
    assertDriverMarket(e2.driverMarket().get(0), "16", "Charles Leclerc");
  }

  @Test
  void getAllEvents_whenNoSessions_returnsEmpty_andSkipsDriversFetch() {
    when(openF1Client.fetchSessions("Race", "2024", "Austria"))
        .thenReturn(new Session[0]);

    List<EventDetails> result = eventsProvider.getAllEvents("Race", "2024", "Austria");

    assertThat(result).isEmpty();
    verify(openF1Client, never()).fetchDriversBySession(anyString());
  }


  private static void assertDriverMarket(DriverMarket dm, String expectedId, String expectedName) {
    assertThat(dm.driverId()).isEqualTo(expectedId);
    assertThat(dm.fullName()).isEqualTo(expectedName);
    assertThat(dm.odds()).isBetween(2, 4);
  }

  private static Session session(String key, String type, String year, String country) {
    return new Session(
        "circuitKey",        // circuit_key
        "circuitShort",      // circuit_short_name
        "AT",                // country_code
        "cc",                // country_key
        country,             // country_name
        "2024-07-01T16:00Z", // date_end
        "2024-07-01T14:00Z", // date_start
        "+00:00",            // gmt_offset
        "Spielberg",         // location
        "mtgKey",            // meeting_key
        key,                 // session_key   <- used
        "Race Name",         // session_name
        type,                // session_type  <- used
        year                 // year          <- used
    );
  }
}
