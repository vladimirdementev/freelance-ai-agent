package com.freelanceai.agent.project;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "projects",
        uniqueConstraints = @UniqueConstraint(name = "uk_projects_platform_external_id", columnNames = {"platform", "external_id"})
)
public class FreelanceProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ProjectPlatform platform;

    @Column(name = "external_id", nullable = false, length = 120)
    private String externalId;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @Column(precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "source_url", length = 1000)
    private String sourceUrl;

    @Column(name = "source_category", length = 300)
    private String sourceCategory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ProjectCategory category = ProjectCategory.OTHER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ProjectComplexity complexity = ProjectComplexity.UNKNOWN;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "project_technologies", joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "technology", nullable = false, length = 80)
    private Set<String> technologies = new LinkedHashSet<>();

    @Column(name = "estimated_hours")
    private Integer estimatedHours;

    @Column(name = "automation_percent")
    private Integer automationPercent;

    @Column(name = "skill_match_percent")
    private Integer skillMatchPercent;

    @Column(name = "risk_percent")
    private Integer riskPercent;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal score = BigDecimal.ZERO;

    @Column(name = "notified_at")
    private Instant notifiedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public ProjectPlatform getPlatform() {
        return platform;
    }

    public void setPlatform(ProjectPlatform platform) {
        this.platform = platform;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getSourceCategory() {
        return sourceCategory;
    }

    public void setSourceCategory(String sourceCategory) {
        this.sourceCategory = sourceCategory;
    }

    public ProjectCategory getCategory() {
        return category;
    }

    public void setCategory(ProjectCategory category) {
        this.category = category;
    }

    public ProjectComplexity getComplexity() {
        return complexity;
    }

    public void setComplexity(ProjectComplexity complexity) {
        this.complexity = complexity;
    }

    public Set<String> getTechnologies() {
        return technologies;
    }

    public void setTechnologies(Set<String> technologies) {
        this.technologies = technologies;
    }

    public Integer getEstimatedHours() {
        return estimatedHours;
    }

    public void setEstimatedHours(Integer estimatedHours) {
        this.estimatedHours = estimatedHours;
    }

    public Integer getAutomationPercent() {
        return automationPercent;
    }

    public void setAutomationPercent(Integer automationPercent) {
        this.automationPercent = automationPercent;
    }

    public Integer getSkillMatchPercent() {
        return skillMatchPercent;
    }

    public void setSkillMatchPercent(Integer skillMatchPercent) {
        this.skillMatchPercent = skillMatchPercent;
    }

    public Integer getRiskPercent() {
        return riskPercent;
    }

    public void setRiskPercent(Integer riskPercent) {
        this.riskPercent = riskPercent;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public Instant getNotifiedAt() {
        return notifiedAt;
    }

    public void setNotifiedAt(Instant notifiedAt) {
        this.notifiedAt = notifiedAt;
    }
}
