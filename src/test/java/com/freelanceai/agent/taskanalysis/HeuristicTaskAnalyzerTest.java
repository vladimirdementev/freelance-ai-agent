package com.freelanceai.agent.taskanalysis;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.freelanceai.agent.project.FreelanceProject;
import com.freelanceai.agent.project.ProjectCategory;
import com.freelanceai.agent.project.ProjectComplexity;
import com.freelanceai.agent.project.ProjectPlatform;

class HeuristicTaskAnalyzerTest {

    private final HeuristicTaskAnalyzer analyzer = new HeuristicTaskAnalyzer();

    @Test
    void createsExecutableTaskAnalysisForTelegramBotProject() {
        FreelanceProject project = new FreelanceProject();
        project.setPlatform(ProjectPlatform.FREELANCER);
        project.setExternalId("projects-chatbot-development-telegram-bot-with-payments");
        project.setTitle("Telegram bot with payments");
        project.setDescription("Need Telegram bot with catalog, payment API and admin notifications.");
        project.setCategory(ProjectCategory.TELEGRAM_BOT);
        project.setComplexity(ProjectComplexity.MEDIUM);
        project.setTechnologies(new LinkedHashSet<>(Set.of("telegram", "payments", "api")));

        TaskAnalysisResult result = analyzer.analyze(project);

        assertThat(result.analyzer()).isEqualTo("heuristic");
        assertThat(result.requirements()).anyMatch(item -> item.contains("Telegram"));
        assertThat(result.questions()).isNotEmpty();
        assertThat(result.risks()).isNotEmpty();
        assertThat(result.implementationPlan()).hasSizeGreaterThanOrEqualTo(5);
        assertThat(result.acceptanceCriteria()).isNotEmpty();
    }
}
