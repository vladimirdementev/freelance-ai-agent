package com.freelanceai.agent.workspace;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.freelanceai.agent.project.FreelanceProject;
import com.freelanceai.agent.project.ProjectCategory;
import com.freelanceai.agent.project.ProjectComplexity;
import com.freelanceai.agent.project.ProjectPlatform;
import com.freelanceai.agent.taskanalysis.TaskAnalysisResponse;

class WorkspaceFileWriterTest {

    private final WorkspaceFileWriter writer = new WorkspaceFileWriter();

    @TempDir
    private Path tempDir;

    @Test
    void writesExecutionWorkspaceMarkdownFiles() throws Exception {
        FreelanceProject project = new FreelanceProject();
        project.setPlatform(ProjectPlatform.FL_RU);
        project.setExternalId("5517886");
        project.setTitle("Telegram bot with payments");
        project.setDescription("Need a Telegram bot with payment API.");
        project.setSourceUrl("https://www.fl.ru/projects/5517886/test.html");
        project.setSourceCategory("Разработка / Чат-боты");
        project.setPrice(BigDecimal.valueOf(18_000));
        project.setCategory(ProjectCategory.TELEGRAM_BOT);
        project.setComplexity(ProjectComplexity.MEDIUM);
        project.setTechnologies(Set.of("telegram", "payments"));
        project.setEstimatedHours(10);
        project.setAutomationPercent(85);
        project.setSkillMatchPercent(90);
        project.setRiskPercent(25);
        project.setScore(BigDecimal.valueOf(88));

        TaskAnalysisResponse analysis = new TaskAnalysisResponse(
                1L,
                10L,
                List.of("Implement Telegram bot"),
                List.of("Which payment provider should be used?"),
                List.of("Payment API access may be missing"),
                List.of("Create bot skeleton", "Add payment flow"),
                List.of("Bot starts from README instructions"),
                "heuristic",
                Instant.parse("2026-08-14T12:00:00Z")
        );

        List<String> files = writer.write(tempDir, project, analysis);

        assertThat(files).contains(
                "task.md",
                "requirements.md",
                "questions.md",
                "risks.md",
                "implementation-plan.md",
                "acceptance-criteria.md",
                "README.md"
        );
        assertThat(Files.readString(tempDir.resolve("task.md"))).contains("Telegram bot with payments");
        assertThat(Files.readString(tempDir.resolve("implementation-plan.md"))).contains("Create bot skeleton");
    }
}
