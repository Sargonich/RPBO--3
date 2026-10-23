package org.example.model;

import jakarta.persistence.*;
import org.hibernate.annotations.Check;

@Entity
@Table(name = "tournaments")
@Check(constraints = "team_limit > 0 AND status IN ('PLANNED', 'IN_PROGRESS', 'FINISHED')")
public class Tournament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(name = "team_limit", nullable = false)
    private int teamLimit;
    @Column(nullable = false, length = 30)
    private String status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTeamLimit() {
        return teamLimit;
    }

    public void setTeamLimit(int teamLimit) {
        this.teamLimit = teamLimit;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}