package com.freelanceai.agent.scoring;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.freelanceai.agent.analysis.ProjectAnalysis;
import com.freelanceai.agent.config.FreelanceAiProperties;
import com.freelanceai.agent.project.ProjectCategory;
import com.freelanceai.agent.project.ProjectComplexity;

class ScoringServiceTest {

    @Test
    void givesHighScoreToProfitableAutomatableProject() {
        FreelanceAiProperties properties = new FreelanceAiProperties();
        properties.getScoring().setTargetHourlyRate(BigDecimal.valueOf(1500));
        ScoringService scoringService = new ScoringService(properties);

        ProjectAnalysis analysis = new ProjectAnalysis(
                ProjectCategory.TELEGRAM_BOT,
                ProjectComplexity.MEDIUM,
                Set.of("telegram", "postgresql", "payments"),
                10,
                85,
                93,
                25
        );

        BigDecimal score = scoringService.score(analysis, BigDecimal.valueOf(18_000));

        assertThat(score).isGreaterThan(BigDecimal.valueOf(80));
    }
}
