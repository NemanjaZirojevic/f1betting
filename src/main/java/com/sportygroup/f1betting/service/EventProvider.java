package com.sportygroup.f1betting.service;

import com.sportygroup.f1betting.model.EventDetails;
import java.util.List;

public interface EventProvider {
  List<EventDetails> getAllEvents(String sessionType, String year, String country);
}
