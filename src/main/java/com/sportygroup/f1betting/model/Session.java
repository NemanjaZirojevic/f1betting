package com.sportygroup.f1betting.model;

public record Session(
    String circuit_key,
    String circuit_short_name,
    String country_code,
    String country_key,
    String country_name,
    String date_end,
    String date_start,
    String gmt_offset,
    String location,
    String meeting_key,
    String session_key,
    String session_name,
    String session_type,
    String year

) {}
