package com.sportygroup.f1betting.model;

public record EventOutcome (
    long eventId,long winnerId,long numberOfWinningBets,long numberOfLostBets
){}
