package org.example.repository;

import jakarta.persistence.LockModeType;
import org.example.annotation.Cumponent;
import org.example.model.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Cumponent
@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Tournament t where t.id = :id")
    Optional<Tournament> findByIdForUpdate(@Param("id") Long id);
}
