package com.freelanceai.agent.taskanalysis;

import java.time.Instant;
import java.util.List;

public record TaskAnalysisResponse(
        Long id,
        Long projectId,
        List<String> requirements,
        List<String> questions,
        List<String> risks,
        List<String> implementationPlan,
        List<String> acceptanceCriteria,
        String analyzer,
        Instant createdAt
) {
}
