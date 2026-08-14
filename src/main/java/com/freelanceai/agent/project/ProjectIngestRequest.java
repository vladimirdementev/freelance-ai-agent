package com.freelanceai.agent.project;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ProjectIngestRequest(
        @NotNull ProjectPlatform platform,
        @NotBlank String externalId,
        @NotBlank String title,
        @NotBlank String description,
        @PositiveOrZero BigDecimal price,
        Instant publishedAt,
        String sourceUrl,
        String sourceCategory
) {
}
