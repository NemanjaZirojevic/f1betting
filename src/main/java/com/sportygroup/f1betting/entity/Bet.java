package com.sportygroup.f1betting.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Getter
@Entity
@Table(
    name = "bets",
    indexes = {
    @Index(name = "ix_bet_event", columnList = "event_id"),
    @Index(name = "ix_bet_status", columnList = "status"),
    @Index(name = "ix_bet_user", columnList = "user_id")
})
public class Bet {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "user_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_bet_user")
  )
  private User user;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "event_id",
      nullable = false,
      foreignKey = @ForeignKey(name="fk_bet_event")
  )
  private Event event;

  @Column(nullable = false)
  private Long driverId;

  @Column(nullable = false)
  private double amount;

  @Column(nullable = false)
  private double odds;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private BetStatus status;

  @Builder.Default
  @Column(nullable = false)
  private Instant createdAt = Instant.now();

}
