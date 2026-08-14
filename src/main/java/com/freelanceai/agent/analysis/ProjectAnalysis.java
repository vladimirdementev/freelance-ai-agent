package com.freelanceai.agent.analysis;

import java.util.Set;

import com.freelanceai.agent.project.ProjectCategory;
import com.freelanceai.agent.project.ProjectComplexity;

public record ProjectAnalysis(
        ProjectCategory category,
        ProjectComplexity complexity,
        Set<String> technologies,
        int estimatedHours,
        int automationPercent,
        int skillMatchPercent,
        int riskPercent
) {
}
