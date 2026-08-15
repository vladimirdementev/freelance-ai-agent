package com.freelanceai.agent.execution;

import java.time.Instant;

import com.freelanceai.agent.project.FreelanceProject;
import com.freelanceai.agent.workspace.ProjectWorkspace;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "execution_runs")
public class ExecutionRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private FreelanceProject project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private ProjectWorkspace workspace;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ExecutionRunStatus status = ExecutionRunStatus.READY_FOR_AGENT;

    @Column(name = "prompt_path", nullable = false, length = 1000)
    private String promptPath;

    @Column(name = "logs_path", length = 1000)
    private String logsPath;

    @Column(name = "result_path", length = 1000)
    private String resultPath;

    @Column(columnDefinition = "text")
    private String summary;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

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

    public ProjectWorkspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(ProjectWorkspace workspace) {
        this.workspace = workspace;
    }

    public ExecutionRunStatus getStatus() {
        return status;
    }

    public void setStatus(ExecutionRunStatus status) {
        this.status = status;
    }

    public String getPromptPath() {
        return promptPath;
    }

    public void setPromptPath(String promptPath) {
        this.promptPath = promptPath;
    }

    public String getLogsPath() {
        return logsPath;
    }

    public void setLogsPath(String logsPath) {
        this.logsPath = logsPath;
    }

    public String getResultPath() {
        return resultPath;
    }

    public void setResultPath(String resultPath) {
        this.resultPath = resultPath;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }
}
