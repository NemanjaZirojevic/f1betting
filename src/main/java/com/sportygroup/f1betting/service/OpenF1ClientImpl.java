package com.sportygroup.f1betting.service;

import com.sportygroup.f1betting.model.Driver;
import com.sportygroup.f1betting.model.Session;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class OpenF1ClientImpl implements OpenF1Client{

  @Value("${event.api.url}")
  private String apiUrl;

  private final RestTemplate restTemplate;

  @RateLimiter(name = "openf1")
  @Retry(name = "openf1", fallbackMethod = "sessionsFallback")
  public Session[] fetchSessions(String sessionType, String year, String country) {
    String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
        .path("/sessions")
        .queryParamIfPresent("session_type", Optional.ofNullable(sessionType))
        .queryParamIfPresent("year",         Optional.ofNullable(year))
        .queryParamIfPresent("country_name", Optional.ofNullable(country))
        .toUriString();

    var resp = restTemplate.getForEntity(url, Session[].class);
    return Optional.ofNullable(resp.getBody()).orElseGet(() -> new Session[0]);
  }

  @RateLimiter(name = "openf1")
  @Retry(name = "openf1", fallbackMethod = "driversFallback")
  public Driver[] fetchDriversBySession(String sessionKey) {
    String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
        .path("/drivers")
        .queryParam("session_key", sessionKey)
        .toUriString();

    var resp = restTemplate.getForEntity(url, Driver[].class);
    return Optional.ofNullable(resp.getBody()).orElseGet(() -> new Driver[0]);
  }

  @SuppressWarnings("unused")
  private Session[] sessionsFallback(String sessionType, String year, String country, Throwable t) {
    return new Session[0];
  }

  @SuppressWarnings("unused")
  private Driver[] driversFallback(String sessionKey, Throwable t) {
    return new Driver[0];
  }
}
