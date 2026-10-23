package org.example.service;

import jakarta.persistence.EntityManager;
import org.example.dto.TeamApplicationRequest;
import org.example.model.Team;
import org.example.model.TeamApplication;
import org.example.model.Tournament;
import org.example.repository.TeamApplicationRepository;
import org.example.repository.TeamRepository;
import org.example.repository.TournamentRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TeamApplicationServiceValidationTest {

    @Test
    void cannotCreateApplicationAfterTournamentStarts() {
        TeamApplicationRepository applications = mock(TeamApplicationRepository.class);
        TeamRepository teams = mock(TeamRepository.class);
        TournamentRepository tournaments = mock(TournamentRepository.class);
        EntityManager entityManager = mock(EntityManager.class);
        Tournament tournament = tournament("IN_PROGRESS");
        when(tournaments.findByIdForUpdate(1L)).thenReturn(Optional.of(tournament));
        TeamApplicationService service = new TeamApplicationService(applications, teams, tournaments, entityManager);

        assertThrows(IllegalArgumentException.class,
                () -> service.createApplication(request()));
        verify(tournaments).findByIdForUpdate(1L);
        verifyNoInteractions(teams);
        verify(applications, never()).save(any());
    }

    @Test
    void createsPendingApplicationForPlannedTournament() {
        TeamApplicationRepository applications = mock(TeamApplicationRepository.class);
        TeamRepository teams = mock(TeamRepository.class);
        TournamentRepository tournaments = mock(TournamentRepository.class);
        EntityManager entityManager = mock(EntityManager.class);
        Tournament tournament = tournament("PLANNED");
        Team team = new Team();
        team.setId(2L);
        team.setName("Test team");
        when(tournaments.findByIdForUpdate(1L)).thenReturn(Optional.of(tournament));
        when(teams.findByIdForUpdate(2L)).thenReturn(Optional.of(team));
        when(applications.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        TeamApplicationService service = new TeamApplicationService(applications, teams, tournaments, entityManager);

        TeamApplication created = service.createApplication(request());

        assertEquals("PENDING", created.getAdmissionStatus());
        assertEquals(1L, created.getTournament().getId());
        assertEquals(2L, created.getTeam().getId());
        verify(tournaments).findByIdForUpdate(1L);
        verify(applications).save(any());
    }

    @Test
    void cannotApproveApplicationThatWasAlreadyRejected() {
        TeamApplicationRepository applications = mock(TeamApplicationRepository.class);
        TeamRepository teams = mock(TeamRepository.class);
        TournamentRepository tournaments = mock(TournamentRepository.class);
        EntityManager entityManager = mock(EntityManager.class);
        Tournament tournament = tournament("PLANNED");
        TeamApplication application = new TeamApplication();
        application.setId(3L);
        application.setTournament(tournament);
        application.setTeam(new Team());
        application.setAdmissionStatus("REJECTED");
        when(applications.findByIdForUpdate(3L)).thenReturn(Optional.of(application));
        when(tournaments.findByIdForUpdate(1L)).thenReturn(Optional.of(tournament));
        TeamApplicationService service = new TeamApplicationService(applications, teams, tournaments, entityManager);

        assertThrows(IllegalArgumentException.class, () -> service.approveApplication(3L));
        verify(applications, never()).save(any());
    }

    @Test
    void cannotApproveWhenTournamentCapacityIsAlreadyFull() {
        TeamApplicationRepository applications = mock(TeamApplicationRepository.class);
        TeamRepository teams = mock(TeamRepository.class);
        TournamentRepository tournaments = mock(TournamentRepository.class);
        EntityManager entityManager = mock(EntityManager.class);
        Tournament tournament = tournament("PLANNED");
        tournament.setTeamLimit(2);
        TeamApplication application = new TeamApplication();
        application.setId(3L);
        application.setTournament(tournament);
        application.setTeam(new Team());
        application.setAdmissionStatus("PENDING");
        when(applications.findByIdForUpdate(3L)).thenReturn(Optional.of(application));
        when(tournaments.findByIdForUpdate(1L)).thenReturn(Optional.of(tournament));
        when(applications.countByTournament_IdAndAdmissionStatus(1L, "APPROVED")).thenReturn(2L);
        TeamApplicationService service = new TeamApplicationService(applications, teams, tournaments, entityManager);

        assertThrows(IllegalArgumentException.class, () -> service.approveApplication(3L));
        verify(applications, never()).save(any());
    }

    @Test
    void cannotMoveApplicationToTournamentThatAlreadyStarted() {
        TeamApplicationRepository applications = mock(TeamApplicationRepository.class);
        TeamRepository teams = mock(TeamRepository.class);
        TournamentRepository tournaments = mock(TournamentRepository.class);
        EntityManager entityManager = mock(EntityManager.class);
        Tournament source = tournament("PLANNED");
        Tournament target = tournament("IN_PROGRESS");
        target.setId(2L);
        Team team = new Team();
        team.setId(2L);
        TeamApplication application = new TeamApplication();
        application.setId(3L);
        application.setTournament(source);
        application.setTeam(team);
        application.setAdmissionStatus("PENDING");
        when(applications.findByIdForUpdate(3L)).thenReturn(Optional.of(application));
        when(tournaments.findByIdForUpdate(1L)).thenReturn(Optional.of(source));
        when(tournaments.findByIdForUpdate(2L)).thenReturn(Optional.of(target));
        when(teams.findByIdForUpdate(2L)).thenReturn(Optional.of(team));
        TeamApplicationService service = new TeamApplicationService(applications, teams, tournaments, entityManager);
        TeamApplicationRequest targetRequest = request();
        targetRequest.setTournamentId(2L);

        assertThrows(IllegalArgumentException.class, () -> service.updateApplication(3L, targetRequest));
        verify(applications, never()).save(any());
    }

    @Test
    void cannotDeleteApprovedApplication() {
        TeamApplicationRepository applications = mock(TeamApplicationRepository.class);
        TeamRepository teams = mock(TeamRepository.class);
        TournamentRepository tournaments = mock(TournamentRepository.class);
        EntityManager entityManager = mock(EntityManager.class);
        Tournament tournament = tournament("PLANNED");
        TeamApplication application = new TeamApplication();
        application.setId(3L);
        application.setTournament(tournament);
        application.setTeam(new Team());
        application.setAdmissionStatus("APPROVED");
        when(applications.findByIdForUpdate(3L)).thenReturn(Optional.of(application));
        when(tournaments.findByIdForUpdate(1L)).thenReturn(Optional.of(tournament));
        TeamApplicationService service = new TeamApplicationService(applications, teams, tournaments, entityManager);

        assertThrows(IllegalArgumentException.class, () -> service.deleteApplication(3L));
        verify(applications, never()).delete(any(TeamApplication.class));
    }

    @Test
    void cannotPatchApplicationAfterTournamentStarts() {
        TeamApplicationRepository applications = mock(TeamApplicationRepository.class);
        TeamRepository teams = mock(TeamRepository.class);
        TournamentRepository tournaments = mock(TournamentRepository.class);
        EntityManager entityManager = mock(EntityManager.class);
        Tournament tournament = tournament("IN_PROGRESS");
        TeamApplication application = new TeamApplication();
        application.setId(3L);
        application.setTournament(tournament);
        application.setTeam(new Team());
        application.setAdmissionStatus("PENDING");
        when(applications.findByIdForUpdate(3L)).thenReturn(Optional.of(application));
        when(tournaments.findByIdForUpdate(1L)).thenReturn(Optional.of(tournament));
        TeamApplicationService service = new TeamApplicationService(applications, teams, tournaments, entityManager);

        assertThrows(IllegalArgumentException.class, () -> service.patchApplication(3L, new TeamApplicationRequest()));
        verify(applications, never()).save(any());
    }

    private static TeamApplicationRequest request() {
        TeamApplicationRequest request = new TeamApplicationRequest();
        request.setTeamId(2L);
        request.setTournamentId(1L);
        return request;
    }

    private static Tournament tournament(String status) {
        Tournament tournament = new Tournament();
        tournament.setId(1L);
        tournament.setName("Test tournament");
        tournament.setTeamLimit(4);
        tournament.setStatus(status);
        return tournament;
    }
}
