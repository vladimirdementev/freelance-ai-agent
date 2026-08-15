package com.freelanceai.agent.execution;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.stereotype.Component;

import com.freelanceai.agent.project.FreelanceProject;
import com.freelanceai.agent.workspace.ProjectWorkspace;

@Component
public class ExecutionPromptWriter {

    public Path writePrompt(Path workspacePath, FreelanceProject project, ProjectWorkspace workspace) {
        try {
            Files.createDirectories(workspacePath);
            Files.createDirectories(workspacePath.resolve("implementation"));
            Path promptPath = workspacePath.resolve("execution-prompt.md");
            Files.writeString(promptPath, prompt(project, workspace), StandardCharsets.UTF_8);
            return promptPath;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write execution prompt for workspace: " + workspacePath, e);
        }
    }

    private String prompt(FreelanceProject project, ProjectWorkspace workspace) {
        return """
                # Coding agent execution prompt

                You are a coding agent working on one freelance project.

                ## Safety rules

                - Do not contact the customer.
                - Do not submit or deliver anything externally.
                - Do not use credentials unless they are explicitly provided inside the workspace.
                - Keep all generated implementation files inside `implementation/` unless the human reviewer instructs otherwise.
                - Prefer a small, working solution over a large unfinished system.

                ## Project

                - Platform: %s
                - External ID: %s
                - Source URL: %s
                - Workspace ID: %s

                ## Required reading

                Read these files before writing code:

                1. `task.md`
                2. `requirements.md`
                3. `questions.md`
                4. `risks.md`
                5. `implementation-plan.md`
                6. `acceptance-criteria.md`

                ## Execution goal

                Create a first implementation draft in `implementation/`.

                The result should include, when relevant:

                - source code;
                - tests or a clear manual verification script;
                - README with setup and run instructions;
                - Dockerfile or docker-compose.yml if the task needs services;
                - a short implementation summary.

                ## Completion checklist

                Before finishing:

                - run available tests/build commands;
                - document commands that were run;
                - list known limitations;
                - list questions that still require human/customer clarification;
                - do not mark the customer delivery as complete.
                """.formatted(
                project.getPlatform(),
                project.getExternalId(),
                project.getSourceUrl() == null ? "-" : project.getSourceUrl(),
                workspace.getId()
        );
    }
}
