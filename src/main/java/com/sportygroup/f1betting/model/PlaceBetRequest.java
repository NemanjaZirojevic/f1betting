package com.sportygroup.f1betting.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;


public record PlaceBetRequest (
  @NotNull Long userId,
  @NotNull Long eventId,
  @NotNull Long driverId,
  @Min(1) double odds,
  @Min(1) double amount
){}
