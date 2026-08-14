package com.freelanceai.agent.taskanalysis;

import java.util.List;

public record TaskAnalysisResult(
        List<String> requirements,
        List<String> questions,
        List<String> risks,
        List<String> implementationPlan,
        List<String> acceptanceCriteria,
        String analyzer
) {
}
