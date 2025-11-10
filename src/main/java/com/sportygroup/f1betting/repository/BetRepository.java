package com.sportygroup.f1betting.repository;

import com.sportygroup.f1betting.entity.Bet;
import com.sportygroup.f1betting.entity.BetStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BetRepository extends JpaRepository<Bet, Integer> {

  List<Bet> findByEvent_IdAndStatus(Long eventId, BetStatus betStatus);

  long countByEvent_IdAndStatus(Long eventId, BetStatus betStatus);

  boolean existsByUser_IdAndEvent_Id(Long userId, Long eventId);

  Optional<Bet> findByEventId(Long eventId);

}
