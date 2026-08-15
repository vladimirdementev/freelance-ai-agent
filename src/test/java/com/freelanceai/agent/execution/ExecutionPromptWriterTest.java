package com.freelanceai.agent.execution;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.freelanceai.agent.project.FreelanceProject;
import com.freelanceai.agent.project.ProjectPlatform;
import com.freelanceai.agent.workspace.ProjectWorkspace;

class ExecutionPromptWriterTest {

    private final ExecutionPromptWriter writer = new ExecutionPromptWriter();

    @TempDir
    private Path tempDir;

    @Test
    void writesExecutionPromptForCodingAgent() throws Exception {
        FreelanceProject project = new FreelanceProject();
        project.setPlatform(ProjectPlatform.FL_RU);
        project.setExternalId("5517886");
        project.setSourceUrl("https://www.fl.ru/projects/5517886/test.html");

        ProjectWorkspace workspace = new ProjectWorkspace();
        workspace.setPath(tempDir.toString());

        Path promptPath = writer.writePrompt(tempDir, project, workspace);

        assertThat(promptPath.getFileName().toString()).isEqualTo("execution-prompt.md");
        assertThat(Files.exists(tempDir.resolve("implementation"))).isTrue();
        assertThat(Files.readString(promptPath))
                .contains("You are a coding agent")
                .contains("Do not contact the customer")
                .contains("implementation/")
                .contains("task.md")
                .contains("acceptance-criteria.md");
    }
}
