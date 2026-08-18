package com.freelanceai.agent.execution;

import java.nio.file.Path;
import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.freelanceai.agent.config.FreelanceAiProperties;
import com.freelanceai.agent.project.FreelanceProject;
import com.freelanceai.agent.project.FreelanceProjectRepository;
import com.freelanceai.agent.workspace.ProjectWorkspace;
import com.freelanceai.agent.workspace.ProjectWorkspaceRepository;
import com.freelanceai.agent.workspace.ProjectWorkspaceResponse;
import com.freelanceai.agent.workspace.ProjectWorkspaceService;

@Service
public class ExecutionRunService {

    private final FreelanceProjectRepository projectRepository;
    private final ProjectWorkspaceRepository workspaceRepository;
    private final ProjectWorkspaceService workspaceService;
    private final ExecutionRunRepository executionRunRepository;
    private final ExecutionPromptWriter promptWriter;
    private final ExecutionRunWorker executionRunWorker;
    private final FreelanceAiProperties properties;

    public ExecutionRunService(
            FreelanceProjectRepository projectRepository,
            ProjectWorkspaceRepository workspaceRepository,
            ProjectWorkspaceService workspaceService,
            ExecutionRunRepository executionRunRepository,
            ExecutionPromptWriter promptWriter,
            ExecutionRunWorker executionRunWorker,
            FreelanceAiProperties properties
    ) {
        this.projectRepository = projectRepository;
        this.workspaceRepository = workspaceRepository;
        this.workspaceService = workspaceService;
        this.executionRunRepository = executionRunRepository;
        this.promptWriter = promptWriter;
        this.executionRunWorker = executionRunWorker;
        this.properties = properties;
    }

    public ExecutionRunResponse createRun(Long projectId) {
        FreelanceProject project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
        ProjectWorkspace workspace = latestOrCreateWorkspace(projectId);

        Path workspacePath = Path.of(workspace.getPath()).toAbsolutePath().normalize();
        Path promptPath = promptWriter.writePrompt(workspacePath, project, workspace);
        Path logsPath = workspacePath.resolve("execution.log");
        Path resultPath = workspacePath.resolve("implementation");

        ExecutionRun run = new ExecutionRun();
        run.setProject(project);
        run.setWorkspace(workspace);
        run.setStatus(ExecutionRunStatus.READY_FOR_AGENT);
        run.setPromptPath(promptPath.toString());
        run.setLogsPath(logsPath.toString());
        run.setResultPath(resultPath.toString());
        run.setSummary("Execution prompt is ready for a coding agent.");

        return toResponse(executionRunRepository.save(run));
    }

    public ExecutionRunResponse startRun(Long projectId, Long runId) {
        if (!StringUtils.hasText(properties.getExecution().getAgentCommand())) {
            throw new IllegalStateException("Execution agent command is not configured.");
        }

        ExecutionRun run = executionRunRepository.findByIdAndProjectId(runId, projectId)
                .orElseThrow(() -> new IllegalArgumentException("Execution run not found: " + runId));
        if (run.getStatus() != ExecutionRunStatus.READY_FOR_AGENT) {
            throw new IllegalStateException("Execution run is not ready for agent: " + run.getStatus());
        }

        run.setStatus(ExecutionRunStatus.RUNNING);
        run.setStartedAt(Instant.now());
        run.setSummary("Execution agent started.");
        ExecutionRun saved = executionRunRepository.save(run);
        executionRunWorker.execute(saved.getId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ExecutionRunResponse latest(Long projectId) {
        return executionRunRepository.findFirstByProjectIdOrderByCreatedAtDesc(projectId)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Execution run not found for project: " + projectId));
    }

    private ProjectWorkspace latestOrCreateWorkspace(Long projectId) {
        return workspaceRepository.findFirstByProjectIdOrderByCreatedAtDesc(projectId)
                .orElseGet(() -> {
                    ProjectWorkspaceResponse created = workspaceService.createWorkspace(projectId);
                    return workspaceRepository.findById(created.id())
                            .orElseThrow(() -> new IllegalStateException("Created workspace not found: " + created.id()));
                });
    }

    private ExecutionRunResponse toResponse(ExecutionRun run) {
        return new ExecutionRunResponse(
                run.getId(),
                run.getProject().getId(),
                run.getWorkspace().getId(),
                run.getStatus(),
                run.getPromptPath(),
                run.getLogsPath(),
                run.getResultPath(),
                run.getSummary(),
                run.getCreatedAt(),
                run.getStartedAt(),
                run.getFinishedAt()
        );
    }
}
