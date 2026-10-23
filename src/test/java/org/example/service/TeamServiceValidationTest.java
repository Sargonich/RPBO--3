package org.example.service;

import org.example.model.Team;
import org.example.model.User;
import org.example.repository.MatchRepository;
import org.example.repository.TeamApplicationRepository;
import org.example.repository.TeamRepository;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TeamServiceValidationTest {

    @Test
    void cannotAddMemberTwice() {
        TeamRepository teams = mock(TeamRepository.class);
        UserRepository users = mock(UserRepository.class);
        TeamApplicationRepository applications = mock(TeamApplicationRepository.class);
        MatchRepository matches = mock(MatchRepository.class);

        User member = new User();
        member.setId(7L);
        Team team = new Team();
        team.setId(3L);
        team.setName("Test team");
        team.setMembers(new ArrayList<>());
        team.getMembers().add(member);

        when(teams.findByIdForUpdate(3L)).thenReturn(Optional.of(team));
        when(users.findById(7L)).thenReturn(Optional.of(member));
        TeamService service = new TeamService(teams, users, applications, matches);

        assertThrows(IllegalArgumentException.class, () -> service.addMember(3L, 7L));
        verify(teams, never()).save(any());
    }

    @Test
    void cannotDeleteTeamWithApplications() {
        TeamRepository teams = mock(TeamRepository.class);
        UserRepository users = mock(UserRepository.class);
        TeamApplicationRepository applications = mock(TeamApplicationRepository.class);
        MatchRepository matches = mock(MatchRepository.class);
        Team team = new Team();
        team.setId(3L);
        when(teams.findByIdForUpdate(3L)).thenReturn(Optional.of(team));
        when(applications.existsByTeam_Id(3L)).thenReturn(true);
        TeamService service = new TeamService(teams, users, applications, matches);

        assertThrows(IllegalArgumentException.class, () -> service.deleteTeam(3L));
        verify(teams, never()).delete(any(Team.class));
        verify(matches, never()).existsByTeam1_IdOrTeam2_Id(any(), any());
    }
}
