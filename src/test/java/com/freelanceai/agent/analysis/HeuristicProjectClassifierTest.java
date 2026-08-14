package com.freelanceai.agent.analysis;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.freelanceai.agent.project.ProjectCategory;
import com.freelanceai.agent.project.ProjectComplexity;

class HeuristicProjectClassifierTest {

    private final HeuristicProjectClassifier classifier = new HeuristicProjectClassifier();

    @Test
    void classifiesTelegramBotWithPaymentsAsTargetNiche() {
        ProjectAnalysis analysis = classifier.classify(
                "Telegram bot with payments",
                "Need a Telegram bot with product catalog, PostgreSQL, payment API and admin notifications.",
                BigDecimal.valueOf(18_000)
        );

        assertThat(analysis.category()).isEqualTo(ProjectCategory.TELEGRAM_BOT);
        assertThat(analysis.complexity()).isEqualTo(ProjectComplexity.MEDIUM);
        assertThat(analysis.technologies()).contains("telegram", "postgresql", "payments", "api");
        assertThat(analysis.automationPercent()).isGreaterThanOrEqualTo(80);
        assertThat(analysis.skillMatchPercent()).isGreaterThanOrEqualTo(85);
    }
}
