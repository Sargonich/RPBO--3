package org.example.service;

import org.example.annotation.Cumponent;
import org.example.dto.TeamApplicationRequest;
import org.example.factory.TeamApplicationFactory;
import org.example.model.Team;
import org.example.model.TeamApplication;
import org.example.model.Tournament;
import org.example.repository.TeamApplicationRepository;
import org.example.repository.TeamRepository;
import org.example.repository.TournamentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.NoSuchElementException;

@Cumponent
@Service
public class TeamApplicationService {

    private final TeamApplicationRepository applicationRepository;
    private final TeamRepository teamRepository;
    private final TournamentRepository tournamentRepository;
    private final EntityManager entityManager;
    private final TeamApplicationFactory applicationFactory =
            new TeamApplicationFactory();

    public TeamApplicationService(
            TeamApplicationRepository applicationRepository,
            TeamRepository teamRepository,
            TournamentRepository tournamentRepository,
            EntityManager entityManager
    ) {
        this.applicationRepository = applicationRepository;
        this.teamRepository = teamRepository;
        this.tournamentRepository = tournamentRepository;
        this.entityManager = entityManager;
    }

    public List<TeamApplication> getAllApplications() {
        return applicationRepository.findAll();
    }

    public TeamApplication getApplicationById(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException("Заявка не найдена: " + id));
    }

    @Transactional
    public TeamApplication createApplication(TeamApplicationRequest request) {
        validateRequest(request);

        // All operations that change tournament applications lock the tournament
        // before checking its state. startTournament uses the same tournament lock.
        Tournament tournament = lockTournament(request.getTournamentId());
        if (!"PLANNED".equals(tournament.getStatus())) {
            throw new IllegalArgumentException("Нельзя подать заявку после начала турнира");
        }
        Team team = lockTeam(request.getTeamId());

        if (applicationRepository.existsByTeam_IdAndTournament_Id(team.getId(), tournament.getId())) {
            throw new IllegalArgumentException("У команды уже есть заявка на этот турнир");
        }

        TeamApplication application = applicationFactory.create(team, tournament, "PENDING");
        return applicationRepository.save(application);
    }

    @Transactional
    public TeamApplication updateApplication(Long id, TeamApplicationRequest request) {
        validateRequest(request);
        TeamApplication application = lockApplication(id);
        Tournament currentTournament = application.getTournament();
        Long currentTournamentId = currentTournament.getId();
        Long targetTournamentId = request.getTournamentId();
        Tournament targetTournament = lockTournamentsInOrder(currentTournamentId, targetTournamentId, targetTournamentId);
        // Re-read after acquiring the row locks so state checks use current DB values.
        entityManager.refresh(application);
        currentTournament = application.getTournament();

        ensureApplicationEditable(application);
        ensureTournamentPlanned(currentTournament, "Нельзя изменить заявку: исходный турнир уже начат");
        ensureTournamentPlanned(targetTournament, "Нельзя перенести заявку в уже начатый турнир");

        Team team = lockTeam(request.getTeamId());
        validateNoDuplicate(team.getId(), targetTournament.getId(), id);
        application.setTeam(team);
        application.setTournament(targetTournament);
        return applicationRepository.save(application);
    }

    @Transactional
    public TeamApplication patchApplication(Long id, TeamApplicationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Тело запроса отсутствует");
        }
        TeamApplication application = lockApplication(id);
        Long currentTournamentId = application.getTournament().getId();
        Long targetTournamentId = request.getTournamentId() != null
                ? request.getTournamentId() : currentTournamentId;
        Tournament targetTournament = lockTournamentsInOrder(currentTournamentId, targetTournamentId, targetTournamentId);
        entityManager.refresh(application);

        Tournament currentTournament = application.getTournament();
        Team team = request.getTeamId() != null
                ? lockTeam(request.getTeamId()) : application.getTeam();

        ensureApplicationEditable(application);
        ensureTournamentPlanned(currentTournament, "Нельзя изменить заявку: исходный турнир уже начат");
        ensureTournamentPlanned(targetTournament, "Нельзя перенести заявку в уже начатый турнир");
        validateNoDuplicate(team.getId(), targetTournament.getId(), id);

        application.setTeam(team);
        application.setTournament(targetTournament);
        return applicationRepository.save(application);
    }

    @Transactional
    public void deleteApplication(Long id) {
        TeamApplication application = lockApplication(id);
        Long tournamentId = application.getTournament().getId();
        Tournament tournament = lockTournament(tournamentId);
        entityManager.refresh(application);
        ensureApplicationEditable(application);
        ensureTournamentPlanned(tournament, "Нельзя удалить заявку после начала турнира");
        applicationRepository.delete(application);
    }

    @Transactional
    public TeamApplication approveApplication(Long id) {
        TeamApplication application = lockApplication(id);
        Tournament tournament = lockTournament(application.getTournament().getId());
        entityManager.refresh(application);
        if (!"PLANNED".equals(tournament.getStatus())) {
            throw new IllegalArgumentException("Нельзя принять заявку: турнир уже начат или завершён");
        }
        if (!"PENDING".equals(application.getAdmissionStatus())) {
            throw new IllegalArgumentException("Рассматривать можно только ожидающую заявку");
        }
        long approved = applicationRepository.countByTournament_IdAndAdmissionStatus(tournament.getId(), "APPROVED");
        if (approved >= tournament.getTeamLimit()) {
            throw new IllegalArgumentException("Лимит команд турнира уже достигнут");
        }
        application.setAdmissionStatus("APPROVED");
        return applicationRepository.save(application);
    }

    @Transactional
    public TeamApplication rejectApplication(Long id) {
        TeamApplication application = lockApplication(id);
        Tournament tournament = lockTournament(application.getTournament().getId());
        entityManager.refresh(application);
        if (!"PLANNED".equals(tournament.getStatus())) {
            throw new IllegalArgumentException("Нельзя отклонить заявку после начала турнира");
        }
        if (!"PENDING".equals(application.getAdmissionStatus())) {
            throw new IllegalArgumentException("Отклонить можно только ожидающую заявку");
        }
        application.setAdmissionStatus("REJECTED");
        return applicationRepository.save(application);
    }

    private TeamApplication lockApplication(Long id) {
        return applicationRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new NoSuchElementException("Заявка не найдена: " + id));
    }

    private Tournament lockTournament(Long id) {
        return tournamentRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new NoSuchElementException("Турнир не найден: " + id));
    }

    private Tournament lockTournamentsInOrder(Long firstId, Long secondId, Long resultId) {
        if (firstId.equals(secondId)) {
            return lockTournament(firstId);
        }
        long low = Math.min(firstId, secondId);
        long high = Math.max(firstId, secondId);
        Tournament lowTournament = lockTournament(low);
        Tournament highTournament = lockTournament(high);
        return resultId.equals(low) ? lowTournament : highTournament;
    }

    private void ensureTournamentPlanned(Tournament tournament, String message) {
        if (!"PLANNED".equals(tournament.getStatus())) {
            throw new IllegalArgumentException(message);
        }
    }

    private void ensureApplicationEditable(TeamApplication application) {
        if (!"PENDING".equals(application.getAdmissionStatus())) {
            throw new IllegalArgumentException("Изменять или удалять можно только ожидающую заявку");
        }
    }

    private Team lockTeam(Long id) {
        return teamRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new NoSuchElementException("Команда не найдена: " + id));
    }



    private void validateNoDuplicate(
            Long teamId,
            Long tournamentId,
            Long excludedApplicationId
    ) {
        boolean duplicate = applicationRepository.existsByTeam_IdAndTournament_IdAndIdNot(
                teamId, tournamentId, excludedApplicationId);

        if (duplicate) {
            throw new IllegalArgumentException(
                    "У команды уже есть заявка на этот турнир");
        }
    }

    private void validateRequest(TeamApplicationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Тело запроса отсутствует");
        }

        if (request.getTeamId() == null) {
            throw new IllegalArgumentException("Необходимо указать teamId");
        }

        if (request.getTournamentId() == null) {
            throw new IllegalArgumentException(
                    "Необходимо указать tournamentId");
        }
    }
}