package org.example.controller;

import org.example.annotation.Cumponent;
import org.example.dto.TeamApplicationRequest;
import org.example.model.TeamApplication;
import org.example.service.TeamApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Cumponent
@RestController
@RequestMapping("/api/team-applications")
public class TeamApplicationController {

    private final TeamApplicationService applicationService;

    public TeamApplicationController(
            TeamApplicationService applicationService
    ) {
        this.applicationService = applicationService;
    }

    @GetMapping
    public ResponseEntity<List<TeamApplication>> getAllApplications() {
        return ResponseEntity.ok(applicationService.getAllApplications());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamApplication> getApplicationById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                applicationService.getApplicationById(id)
        );
    }

    @PostMapping
    public ResponseEntity<TeamApplication> createApplication(
            @RequestBody TeamApplicationRequest request
    ) {
        TeamApplication application =
                applicationService.createApplication(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(application);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeamApplication> updateApplication(
            @PathVariable Long id,
            @RequestBody TeamApplicationRequest request
    ) {
        return ResponseEntity.ok(
                applicationService.updateApplication(id, request)
        );
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TeamApplication> patchApplication(
            @PathVariable Long id,
            @RequestBody TeamApplicationRequest request
    ) {
        return ResponseEntity.ok(
                applicationService.patchApplication(id, request)
        );
    }


    @PostMapping("/{id}/approve")
    public ResponseEntity<TeamApplication> approve(@PathVariable Long id) {
        return ResponseEntity.ok(applicationService.approveApplication(id));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<TeamApplication> reject(@PathVariable Long id) {
        return ResponseEntity.ok(applicationService.rejectApplication(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id) {
        applicationService.deleteApplication(id);
        return ResponseEntity.noContent().build();
    }
}