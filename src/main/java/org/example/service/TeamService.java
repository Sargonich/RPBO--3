package org.example.service;

import org.example.annotation.Cumponent;
import org.example.dto.TeamRequest;
import org.example.factory.TeamFactory;
import org.example.model.Team;
import org.example.model.User;
import org.example.repository.MatchRepository;
import org.example.repository.TeamApplicationRepository;
import org.example.repository.TeamRepository;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Cumponent
@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final TeamApplicationRepository applicationRepository;
    private final MatchRepository matchRepository;
    private final TeamFactory teamFactory = new TeamFactory();

    public TeamService(
            TeamRepository teamRepository,
            UserRepository userRepository,
            TeamApplicationRepository applicationRepository,
            MatchRepository matchRepository
    ) {
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
        this.applicationRepository = applicationRepository;
        this.matchRepository = matchRepository;
    }

    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    public Team getTeamById(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException("Команда не найдена: " + id));
    }

    public Team createTeam(TeamRequest request) {
        validateRequest(request);

        User captain = userRepository.findById(request.getCaptainId())
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Капитан не найден: " + request.getCaptainId()));

        Team team = teamFactory.create(request.getName(), captain);
        team.getMembers().add(captain);

        return teamRepository.save(team);
    }

    @Transactional
    public Team updateTeam(Long id, TeamRequest request) {
        validateRequest(request);

        Team team = teamRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new NoSuchElementException("Команда не найдена: " + id));

        User captain = userRepository.findById(request.getCaptainId())
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Капитан не найден: " + request.getCaptainId()));

        team.setName(request.getName());
        team.setCaptain(captain);

        if (!team.getMembers().contains(captain)) {
            team.getMembers().add(captain);
        }

        return teamRepository.save(team);
    }

    @Transactional
    public Team patchTeam(Long id, TeamRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Тело запроса отсутствует");
        }

        Team team = teamRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new NoSuchElementException("Команда не найдена: " + id));

        if (request.getName() != null) {
            validateName(request.getName());
            team.setName(request.getName());
        }

        if (request.getCaptainId() != null) {
            User captain = userRepository.findById(request.getCaptainId())
                    .orElseThrow(() ->
                            new NoSuchElementException(
                                    "Капитан не найден: " + request.getCaptainId()));

            team.setCaptain(captain);

            if (!team.getMembers().contains(captain)) {
                team.getMembers().add(captain);
            }
        }

        return teamRepository.save(team);
    }


    @Transactional
    public Team addMember(Long teamId, Long userId) {
        Team team = teamRepository.findByIdForUpdate(teamId)
                .orElseThrow(() -> new NoSuchElementException("Команда не найдена: " + teamId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден: " + userId));
        if (team.getMembers().stream().anyMatch(member -> member.getId().equals(userId))) {
            throw new IllegalArgumentException("Пользователь уже состоит в команде");
        }
        team.getMembers().add(user);
        return teamRepository.save(team);
    }

    @Transactional
    public void deleteTeam(Long id) {
        Team team = teamRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new NoSuchElementException("Команда не найдена: " + id));

        boolean hasApplications = applicationRepository.existsByTeam_Id(id);

        if (hasApplications) {
            throw new IllegalArgumentException(
                    "Нельзя удалить команду: существуют связанные заявки");
        }

        boolean hasMatches = matchRepository.existsByTeam1_IdOrTeam2_Id(id, id);

        if (hasMatches) {
            throw new IllegalArgumentException(
                    "Нельзя удалить команду: существуют связанные матчи");
        }

        teamRepository.delete(team);
    }

    private void validateRequest(TeamRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Тело запроса отсутствует");
        }

        validateName(request.getName());

        if (request.getCaptainId() == null) {
            throw new IllegalArgumentException("Необходимо указать captainId");
        }
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Название команды не может быть пустым");
        }
    }
}