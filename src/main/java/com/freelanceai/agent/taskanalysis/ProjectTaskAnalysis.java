package com.freelanceai.agent.taskanalysis;

import java.time.Instant;

import com.freelanceai.agent.project.FreelanceProject;

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
@Table(name = "project_task_analyses")
public class ProjectTaskAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private FreelanceProject project;

    @Column(nullable = false, columnDefinition = "text")
    private String requirements;

    @Column(nullable = false, columnDefinition = "text")
    private String questions;

    @Column(nullable = false, columnDefinition = "text")
    private String risks;

    @Column(name = "implementation_plan", nullable = false, columnDefinition = "text")
    private String implementationPlan;

    @Column(name = "acceptance_criteria", nullable = false, columnDefinition = "text")
    private String acceptanceCriteria;

    @Column(nullable = false, length = 80)
    private String analyzer;

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

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public String getQuestions() {
        return questions;
    }

    public void setQuestions(String questions) {
        this.questions = questions;
    }

    public String getRisks() {
        return risks;
    }

    public void setRisks(String risks) {
        this.risks = risks;
    }

    public String getImplementationPlan() {
        return implementationPlan;
    }

    public void setImplementationPlan(String implementationPlan) {
        this.implementationPlan = implementationPlan;
    }

    public String getAcceptanceCriteria() {
        return acceptanceCriteria;
    }

    public void setAcceptanceCriteria(String acceptanceCriteria) {
        this.acceptanceCriteria = acceptanceCriteria;
    }

    public String getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(String analyzer) {
        this.analyzer = analyzer;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
