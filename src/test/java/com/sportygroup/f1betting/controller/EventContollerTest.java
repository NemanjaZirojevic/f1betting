package com.sportygroup.f1betting.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportygroup.f1betting.model.DriverMarket;
import com.sportygroup.f1betting.model.EventDetails;
import com.sportygroup.f1betting.model.EventOutcome;
import com.sportygroup.f1betting.model.EventOutcomeRequest;
import com.sportygroup.f1betting.service.BettingService;
import com.sportygroup.f1betting.service.EventService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = EventController.class)
class EventControllerTest {

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper objectMapper;

  @MockitoBean EventService eventService;
  @MockitoBean BettingService bettingService;

  @Test
  void list_returnsEvents_withFilters() throws Exception {
    var details = List.of(
        new EventDetails("k1", "Race", "2024", "Austria",
            List.of(new DriverMarket("44", "Lewis Hamilton", 3))),
        new EventDetails("k2", "Race", "2024", "Austria",
            List.of(new DriverMarket("1", "Max Verstappen", 2)))
    );

    when(eventService.findEvents("Race", "2024", "Austria")).thenReturn(details);

    mvc.perform(get("/api/events")
            .param("sessionType", "Race")
            .param("year", "2024")
            .param("country", "Austria"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].id", is("k1")))
        .andExpect(jsonPath("$[0].sessionType", is("Race")))
        .andExpect(jsonPath("$[0].year", is("2024")))
        .andExpect(jsonPath("$[0].country", is("Austria")))
        .andExpect(jsonPath("$[0].driverMarket[0].driverId", is("44")))
        .andExpect(jsonPath("$[1].id", is("k2")));

    verify(eventService).findEvents("Race", "2024", "Austria");
  }

  @Test
  void list_allowsNullFilters() throws Exception {
    when(eventService.findEvents(null, null, null)).thenReturn(List.of());

    mvc.perform(get("/api/events"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));

    verify(eventService).findEvents(null, null, null);
  }

  @Test
  void settle_returnsOutcome_onSuccess() throws Exception {
    long eventId = 77L;
    var request = new EventOutcomeRequest(30L);
    var outcome = new EventOutcome(eventId, 30L, 2L, 5L);

    when(bettingService.settleOutcome(eventId, 30L)).thenReturn(outcome);

    mvc.perform(post("/api/events/{eventId}/outcome", eventId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.eventId", is(77)))
        .andExpect(jsonPath("$.winnerId", is(30)))
        .andExpect(jsonPath("$.numberOfWinningBets", is(2)))
        .andExpect(jsonPath("$.numberOfLostBets", is(5)));
  }

  @Test
  void settle_returns400_onInvalidBody() throws Exception {
    var invalidJson = "{}";

    mvc.perform(post("/api/events/{eventId}/outcome", 55L)
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidJson))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(bettingService);
  }
}
