package org.example.model;

import jakarta.persistence.*;

@Entity
@Table(name = "team_applications", uniqueConstraints = @UniqueConstraint(columnNames = {"team_id", "tournament_id"}))
@org.hibernate.annotations.Check(constraints = "admission_status IN ('PENDING', 'APPROVED', 'REJECTED')")
public class TeamApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;
    @Column(name = "admission_status", nullable = false, length = 30)
    private String admissionStatus;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public Tournament getTournament() {
        return tournament;
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }

    public String getAdmissionStatus() {
        return admissionStatus;
    }

    public void setAdmissionStatus(String admissionStatus) {
        this.admissionStatus = admissionStatus;
    }
}