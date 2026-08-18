package com.freelanceai.agent.execution;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.freelanceai.agent.config.FreelanceAiProperties;

@Service
public class ExecutionRunWorker {

    private static final Logger log = LoggerFactory.getLogger(ExecutionRunWorker.class);

    private final ExecutionRunRepository executionRunRepository;
    private final FreelanceAiProperties properties;

    public ExecutionRunWorker(ExecutionRunRepository executionRunRepository, FreelanceAiProperties properties) {
        this.executionRunRepository = executionRunRepository;
        this.properties = properties;
    }

    @Async
    public void execute(Long runId) {
        ExecutionRun run = executionRunRepository.findById(runId)
                .orElseThrow(() -> new IllegalArgumentException("Execution run not found: " + runId));

        Path promptPath = Path.of(run.getPromptPath()).toAbsolutePath().normalize();
        Path workspacePath = promptPath.getParent();
        Path logsPath = Path.of(run.getLogsPath()).toAbsolutePath().normalize();
        Path resultPath = Path.of(run.getResultPath()).toAbsolutePath().normalize();

        try {
            Files.createDirectories(logsPath.getParent());
            Files.createDirectories(resultPath);
            ProcessBuilder processBuilder = new ProcessBuilder("/bin/sh", "-lc", properties.getExecution().getAgentCommand());
            processBuilder.directory(workspacePath.toFile());
            processBuilder.redirectErrorStream(true);
            processBuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(logsPath.toFile()));
            processBuilder.environment().put("WORKSPACE_PATH", workspacePath.toString());
            processBuilder.environment().put("EXECUTION_PROMPT_PATH", promptPath.toString());
            processBuilder.environment().put("EXECUTION_RESULT_PATH", resultPath.toString());
            processBuilder.environment().put("EXECUTION_LOG_PATH", logsPath.toString());

            Process process = processBuilder.start();
            boolean completed = process.waitFor(timeoutSeconds(), TimeUnit.SECONDS);
            if (!completed) {
                process.destroyForcibly();
                markFinished(runId, ExecutionRunStatus.FAILED, "Execution timed out.");
                return;
            }

            int exitCode = process.exitValue();
            if (exitCode == 0) {
                markFinished(runId, ExecutionRunStatus.SUCCEEDED, "Execution completed successfully.");
            } else {
                markFinished(runId, ExecutionRunStatus.FAILED, "Execution failed with exit code " + exitCode + ".");
            }
        } catch (IOException e) {
            log.warn("Execution run {} failed to start", runId, e);
            markFinished(runId, ExecutionRunStatus.FAILED, "Execution failed to start: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            markFinished(runId, ExecutionRunStatus.FAILED, "Execution was interrupted.");
        } catch (RuntimeException e) {
            log.warn("Execution run {} failed", runId, e);
            markFinished(runId, ExecutionRunStatus.FAILED, "Execution failed: " + e.getMessage());
        }
    }

    private long timeoutSeconds() {
        return Math.max(1L, properties.getExecution().getTimeoutSeconds());
    }

    private void markFinished(Long runId, ExecutionRunStatus status, String summary) {
        executionRunRepository.findById(runId).ifPresent(run -> {
            run.setStatus(status);
            run.setFinishedAt(Instant.now());
            run.setSummary(summary);
            executionRunRepository.save(run);
        });
    }
}
