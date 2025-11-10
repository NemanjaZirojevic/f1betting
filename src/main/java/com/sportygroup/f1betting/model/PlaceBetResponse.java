package com.sportygroup.f1betting.model;

public record PlaceBetResponse (
    Long betId,
    int odds,
    double amount,
    double remainingBalance
){}
