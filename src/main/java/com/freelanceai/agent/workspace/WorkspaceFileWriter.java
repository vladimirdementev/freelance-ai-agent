package com.freelanceai.agent.workspace;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.springframework.stereotype.Component;

import com.freelanceai.agent.project.FreelanceProject;
import com.freelanceai.agent.taskanalysis.TaskAnalysisResponse;

@Component
public class WorkspaceFileWriter {

    public List<String> write(Path workspacePath, FreelanceProject project, TaskAnalysisResponse analysis) {
        try {
            Files.createDirectories(workspacePath);

            writeFile(workspacePath, "task.md", task(project));
            writeFile(workspacePath, "requirements.md", list("Requirements", analysis.requirements()));
            writeFile(workspacePath, "questions.md", list("Questions for customer", analysis.questions()));
            writeFile(workspacePath, "risks.md", list("Risks", analysis.risks()));
            writeFile(workspacePath, "implementation-plan.md", list("Implementation plan", analysis.implementationPlan()));
            writeFile(workspacePath, "acceptance-criteria.md", list("Acceptance criteria", analysis.acceptanceCriteria()));
            writeFile(workspacePath, "README.md", readme(project));

            return List.of(
                    "task.md",
                    "requirements.md",
                    "questions.md",
                    "risks.md",
                    "implementation-plan.md",
                    "acceptance-criteria.md",
                    "README.md"
            );
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write project workspace: " + workspacePath, e);
        }
    }

    private void writeFile(Path workspacePath, String fileName, String content) throws IOException {
        Files.writeString(workspacePath.resolve(fileName), content, StandardCharsets.UTF_8);
    }

    private String task(FreelanceProject project) {
        return """
                # Task

                ## Source

                - Platform: %s
                - External ID: %s
                - Source URL: %s
                - Source category: %s
                - Price: %s RUB
                - Score: %s

                ## Title

                %s

                ## Description

                %s

                ## Detected metadata

                - Category: %s
                - Complexity: %s
                - Technologies: %s
                - Estimated hours: %s
                - Automation: %s%%
                - Skill match: %s%%
                - Risk: %s%%
                """.formatted(
                project.getPlatform(),
                project.getExternalId(),
                value(project.getSourceUrl()),
                value(project.getSourceCategory()),
                project.getPrice() == null ? "not specified" : project.getPrice().stripTrailingZeros().toPlainString(),
                project.getScore(),
                value(project.getTitle()),
                value(project.getDescription()),
                project.getCategory(),
                project.getComplexity(),
                project.getTechnologies().isEmpty() ? "-" : String.join(", ", project.getTechnologies()),
                project.getEstimatedHours(),
                project.getAutomationPercent(),
                project.getSkillMatchPercent(),
                project.getRiskPercent()
        );
    }

    private String list(String title, List<String> items) {
        StringBuilder builder = new StringBuilder("# ").append(title).append("\n\n");
        if (items == null || items.isEmpty()) {
            return builder.append("- Not specified\n").toString();
        }
        for (String item : items) {
            builder.append("- ").append(item).append("\n");
        }
        return builder.toString();
    }

    private String readme(FreelanceProject project) {
        return """
                # Execution workspace

                This folder contains the prepared task package for project `%s` from `%s`.

                Suggested flow:

                1. Read `task.md`.
                2. Review `questions.md` before committing to delivery.
                3. Use `requirements.md`, `implementation-plan.md`, and `acceptance-criteria.md` as the implementation contract.
                4. Put generated code into a separate implementation folder or repository.
                5. Run tests and build before sending anything to the customer.
                """.formatted(project.getExternalId(), project.getPlatform());
    }

    private String value(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
