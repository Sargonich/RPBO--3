package org.example.controller;

import org.example.annotation.Cumponent;
import org.example.dto.TeamRequest;
import org.example.model.Team;
import org.example.service.TeamService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Cumponent
@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping
    public ResponseEntity<List<Team>> getAllTeams() {
        return ResponseEntity.ok(teamService.getAllTeams());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Team> getTeamById(@PathVariable Long id) {
        return ResponseEntity.ok(teamService.getTeamById(id));
    }

    @PostMapping
    public ResponseEntity<Team> createTeam(@RequestBody TeamRequest request) {
        Team team = teamService.createTeam(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(team);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Team> updateTeam(
            @PathVariable Long id,
            @RequestBody TeamRequest request
    ) {
        return ResponseEntity.ok(teamService.updateTeam(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Team> patchTeam(
            @PathVariable Long id,
            @RequestBody TeamRequest request
    ) {
        return ResponseEntity.ok(teamService.patchTeam(id, request));
    }


    @PostMapping("/{teamId}/members/{userId}")
    public ResponseEntity<Team> addMember(@PathVariable Long teamId, @PathVariable Long userId) {
        return ResponseEntity.ok(teamService.addMember(teamId, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeam(@PathVariable Long id) {
        teamService.deleteTeam(id);
        return ResponseEntity.noContent().build();
    }
}