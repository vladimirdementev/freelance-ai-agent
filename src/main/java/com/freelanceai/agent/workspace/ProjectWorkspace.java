package com.freelanceai.agent.workspace;

import java.time.Instant;

import com.freelanceai.agent.project.FreelanceProject;
import com.freelanceai.agent.taskanalysis.ProjectTaskAnalysis;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "project_workspaces")
public class ProjectWorkspace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private FreelanceProject project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_analysis_id")
    private ProjectTaskAnalysis taskAnalysis;

    @Column(nullable = false, length = 1000)
    private String path;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public FreelanceProject getProject() {
        return project;
    }

    public void setProject(FreelanceProject project) {
        this.project = project;
    }

    public ProjectTaskAnalysis getTaskAnalysis() {
        return taskAnalysis;
    }

    public void setTaskAnalysis(ProjectTaskAnalysis taskAnalysis) {
        this.taskAnalysis = taskAnalysis;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
