package com.sportygroup.f1betting.service;

import com.sportygroup.f1betting.model.Driver;
import com.sportygroup.f1betting.model.Session;

public interface OpenF1Client {
  Session[] fetchSessions(String sessionType, String year, String country);
  Driver[]  fetchDriversBySession(String sessionKey);
}
