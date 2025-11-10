package com.sportygroup.f1betting.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportygroup.f1betting.entity.Bet;
import com.sportygroup.f1betting.entity.BetStatus;
import com.sportygroup.f1betting.entity.Event;
import com.sportygroup.f1betting.entity.User;
import com.sportygroup.f1betting.exceptions.DuplicateBetException;
import com.sportygroup.f1betting.exceptions.GlobalExceptionHandler;
import com.sportygroup.f1betting.model.PlaceBetRequest;
import com.sportygroup.f1betting.service.BettingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {BetsController.class, GlobalExceptionHandler.class})
class BetsControllerTest {

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper objectMapper;

  @MockitoBean BettingService bettingService;

  @Test
  void place_returns201_andBetBody_onSuccess() throws Exception {
    // given
    var req = new PlaceBetRequest(10L, 20L, 30L, 2.5, 100.0);

    var bet = Bet.builder()
        .id(999L)
        .user(User.builder().id(10L).balance(500.0).build())
        .event(Event.builder().id(20L).build())
        .driverId(30L)
        .odds(2.5)
        .amount(100.0)
        .status(BetStatus.PENDING)
        .createdAt(Instant.now())
        .build();

    when(bettingService.placeBet(any(PlaceBetRequest.class))).thenReturn(bet);

    // when/then
    mvc.perform(post("/api/bets")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(999)))
        .andExpect(jsonPath("$.driverId", is(30)))
        .andExpect(jsonPath("$.amount", is(100.0)))
        .andExpect(jsonPath("$.odds", is(2.5)))
        .andExpect(jsonPath("$.status", is("PENDING")));
  }

  @Test
  void place_returns400_whenValidationFails() throws Exception {
    var invalidJson = """
            {"userId":10,"eventId":20,"odds":0,"amount":0}
            """;

    mvc.perform(post("/api/bets")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidJson))
        .andExpect(status().isBadRequest());
    verifyNoInteractions(bettingService);
  }

  @Test
  void place_returns400_whenDuplicateBet() throws Exception {
    var req = new PlaceBetRequest(10L, 20L, 30L, 2.0, 50.0);
    when(bettingService.placeBet(any())).thenThrow(
        new DuplicateBetException("User 10 has already placed a bet for event 20")
    );

    mvc.perform(post("/api/bets")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("already placed a bet")));
  }
}