package com.sportygroup.f1betting.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record EventOutcomeRequest (
    @NotNull @Positive Long winnerId
){}
