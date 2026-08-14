package com.freelanceai.agent.collector;

import java.math.BigDecimal;
import java.time.Instant;

import com.freelanceai.agent.project.ProjectIngestRequest;
import com.freelanceai.agent.project.ProjectPlatform;

public record CollectedProject(
        ProjectPlatform platform,
        String externalId,
        String title,
        String description,
        BigDecimal price,
        Instant publishedAt,
        String sourceUrl,
        String sourceCategory
) {

    public ProjectIngestRequest toIngestRequest() {
        return new ProjectIngestRequest(platform, externalId, title, description, price, publishedAt, sourceUrl, sourceCategory);
    }
}
