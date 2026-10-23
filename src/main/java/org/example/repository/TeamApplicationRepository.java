package org.example.repository;

import org.example.annotation.Cumponent;
import org.example.model.TeamApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Cumponent
@Repository
public interface TeamApplicationRepository extends JpaRepository<TeamApplication, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from TeamApplication a where a.id = :id")
    Optional<TeamApplication> findByIdForUpdate(@Param("id") Long id);

    boolean existsByTeam_IdAndTournament_IdAndIdNot(Long teamId, Long tournamentId, Long excludedId);

    boolean existsByTeam_IdAndTournament_Id(Long teamId, Long tournamentId);

    long countByTournament_IdAndAdmissionStatus(Long tournamentId, String admissionStatus);

    boolean existsByTournament_Id(Long tournamentId);

    boolean existsByTeam_Id(Long teamId);

    boolean existsByTournament_IdAndTeam_IdAndAdmissionStatus(
            Long tournamentId, Long teamId, String admissionStatus);
}
