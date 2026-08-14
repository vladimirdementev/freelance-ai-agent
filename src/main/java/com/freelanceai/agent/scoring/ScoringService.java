package com.freelanceai.agent.scoring;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;

import com.freelanceai.agent.analysis.ProjectAnalysis;
import com.freelanceai.agent.config.FreelanceAiProperties;
import com.freelanceai.agent.project.ProjectComplexity;

@Service
public class ScoringService {

    private final FreelanceAiProperties properties;

    public ScoringService(FreelanceAiProperties properties) {
        this.properties = properties;
    }

    public BigDecimal score(ProjectAnalysis analysis, BigDecimal price) {
        double skillMatch = analysis.skillMatchPercent();
        double automation = analysis.automationPercent();
        double pricePerTime = pricePerTimeScore(price, analysis.estimatedHours());
        double simplicity = simplicityScore(analysis.complexity());
        double winProbability = winProbabilityScore(analysis, simplicity);

        double score = 0.30 * skillMatch
                + 0.25 * automation
                + 0.20 * pricePerTime
                + 0.15 * simplicity
                + 0.10 * winProbability;

        return BigDecimal.valueOf(clamp(score)).setScale(2, RoundingMode.HALF_UP);
    }

    private double pricePerTimeScore(BigDecimal price, int estimatedHours) {
        if (price == null || estimatedHours <= 0) {
            return 0;
        }
        BigDecimal hourlyRate = price.divide(BigDecimal.valueOf(estimatedHours), 2, RoundingMode.HALF_UP);
        BigDecimal targetHourlyRate = properties.getScoring().getTargetHourlyRate();
        if (targetHourlyRate == null || targetHourlyRate.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        return clamp(hourlyRate.divide(targetHourlyRate, 4, RoundingMode.HALF_UP).doubleValue() * 100);
    }

    private double simplicityScore(ProjectComplexity complexity) {
        return switch (complexity) {
            case LOW -> 100;
            case MEDIUM -> 70;
            case HIGH -> 35;
            case UNKNOWN -> 50;
        };
    }

    private double winProbabilityScore(ProjectAnalysis analysis, double simplicity) {
        double weightedOpportunity = (analysis.skillMatchPercent() + analysis.automationPercent() + simplicity) / 3.0;
        return clamp(weightedOpportunity - (analysis.riskPercent() * 0.35));
    }

    private double clamp(double value) {
        return Math.max(0, Math.min(100, value));
    }
}
