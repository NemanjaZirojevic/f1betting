package com.sportygroup.f1betting.model;

import java.util.List;

public record EventDetails (
    String id,
    String sessionType,
    String year,
    String country,
    List<DriverMarket> driverMarket
){}
