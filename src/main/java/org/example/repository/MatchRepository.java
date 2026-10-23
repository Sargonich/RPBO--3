package org.example.repository;

import org.example.annotation.Cumponent;
import org.example.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Cumponent
@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Match m where m.id = :id")
    Optional<Match> findByIdForUpdate(@Param("id") Long id);
    boolean existsByTournament_Id(Long tournamentId);
    boolean existsByTeam1_IdOrTeam2_Id(Long team1Id, Long team2Id);
}
