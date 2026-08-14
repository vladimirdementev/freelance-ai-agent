package com.freelanceai.agent.project;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

public record ProjectResponse(
        Long id,
        ProjectPlatform platform,
        String externalId,
        String title,
        String description,
        BigDecimal price,
        Instant publishedAt,
        ProjectCategory category,
        ProjectComplexity complexity,
        Set<String> technologies,
        Integer estimatedHours,
        Integer automationPercent,
        Integer skillMatchPercent,
        Integer riskPercent,
        BigDecimal score
) {

    public static ProjectResponse from(FreelanceProject project) {
        return new ProjectResponse(
                project.getId(),
                project.getPlatform(),
                project.getExternalId(),
                project.getTitle(),
                project.getDescription(),
                project.getPrice(),
                project.getPublishedAt(),
                project.getCategory(),
                project.getComplexity(),
                project.getTechnologies(),
                project.getEstimatedHours(),
                project.getAutomationPercent(),
                project.getSkillMatchPercent(),
                project.getRiskPercent(),
                project.getScore()
        );
    }
}
