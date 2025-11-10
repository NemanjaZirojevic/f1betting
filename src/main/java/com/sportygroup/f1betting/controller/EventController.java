package com.sportygroup.f1betting.controller;

import com.sportygroup.f1betting.model.EventDetails;
import com.sportygroup.f1betting.model.EventOutcome;
import com.sportygroup.f1betting.model.EventOutcomeRequest;
import com.sportygroup.f1betting.service.BettingService;
import com.sportygroup.f1betting.service.EventService;
import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

  private final EventService eventService;
  private final BettingService bettingService;

  @GetMapping
  public List<EventDetails> list(
      @RequestParam(required = false) String sessionType,
      @RequestParam(required = false) String year,
      @RequestParam(required = false) String country
  ) {
    return eventService.findEvents(sessionType, year, country);
  }


  @PostMapping("/{eventId}/outcome")
  public EventOutcome settle(@PathVariable Long eventId, @Valid @RequestBody EventOutcomeRequest req) {
     return bettingService.settleOutcome(eventId, req.winnerId());
  }

}
