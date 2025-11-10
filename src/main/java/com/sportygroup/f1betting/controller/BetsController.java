package com.sportygroup.f1betting.controller;


import com.sportygroup.f1betting.entity.Bet;
import com.sportygroup.f1betting.model.PlaceBetRequest;
import com.sportygroup.f1betting.service.BettingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bets")
@RequiredArgsConstructor
@Validated
public class BetsController {

  private final BettingService bettingService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Bet place(@Valid @RequestBody PlaceBetRequest req) {
    return bettingService.placeBet(req);
  }

}
