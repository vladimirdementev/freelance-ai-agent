package com.freelanceai.agent.workspace;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.freelanceai.agent.config.FreelanceAiProperties;
import com.freelanceai.agent.project.FreelanceProject;
import com.freelanceai.agent.project.FreelanceProjectRepository;
import com.freelanceai.agent.taskanalysis.ProjectTaskAnalysis;
import com.freelanceai.agent.taskanalysis.ProjectTaskAnalysisRepository;
import com.freelanceai.agent.taskanalysis.ProjectTaskAnalysisService;
import com.freelanceai.agent.taskanalysis.TaskAnalysisResponse;

@Service
public class ProjectWorkspaceService {

    private final FreelanceProjectRepository projectRepository;
    private final ProjectTaskAnalysisRepository taskAnalysisRepository;
    private final ProjectTaskAnalysisService taskAnalysisService;
    private final ProjectWorkspaceRepository workspaceRepository;
    private final WorkspaceFileWriter workspaceFileWriter;
    private final FreelanceAiProperties properties;

    public ProjectWorkspaceService(
            FreelanceProjectRepository projectRepository,
            ProjectTaskAnalysisRepository taskAnalysisRepository,
            ProjectTaskAnalysisService taskAnalysisService,
            ProjectWorkspaceRepository workspaceRepository,
            WorkspaceFileWriter workspaceFileWriter,
            FreelanceAiProperties properties
    ) {
        this.projectRepository = projectRepository;
        this.taskAnalysisRepository = taskAnalysisRepository;
        this.taskAnalysisService = taskAnalysisService;
        this.workspaceRepository = workspaceRepository;
        this.workspaceFileWriter = workspaceFileWriter;
        this.properties = properties;
    }

    public ProjectWorkspaceResponse createWorkspace(Long projectId) {
        FreelanceProject project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));

        ProjectTaskAnalysis taskAnalysis = latestOrCreateTaskAnalysis(projectId);
        TaskAnalysisResponse analysis = taskAnalysisService.latest(projectId);

        Path workspacePath = rootPath().resolve(slug(project)).normalize();
        List<String> files = workspaceFileWriter.write(workspacePath, project, analysis);

        ProjectWorkspace workspace = new ProjectWorkspace();
        workspace.setProject(project);
        workspace.setTaskAnalysis(taskAnalysis);
        workspace.setPath(workspacePath.toString());

        return toResponse(workspaceRepository.save(workspace), files);
    }

    @Transactional(readOnly = true)
    public ProjectWorkspaceResponse latest(Long projectId) {
        ProjectWorkspace workspace = workspaceRepository.findFirstByProjectIdOrderByCreatedAtDesc(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found for project: " + projectId));
        return toResponse(workspace, List.of());
    }

    private ProjectTaskAnalysis latestOrCreateTaskAnalysis(Long projectId) {
        return taskAnalysisRepository.findFirstByProjectIdOrderByCreatedAtDesc(projectId)
                .orElseGet(() -> {
                    TaskAnalysisResponse created = taskAnalysisService.analyze(projectId);
                    return taskAnalysisRepository.findById(created.id())
                            .orElseThrow(() -> new IllegalStateException("Created task analysis not found: " + created.id()));
                });
    }

    private ProjectWorkspaceResponse toResponse(ProjectWorkspace workspace, List<String> files) {
        return new ProjectWorkspaceResponse(
                workspace.getId(),
                workspace.getProject().getId(),
                workspace.getTaskAnalysis() == null ? null : workspace.getTaskAnalysis().getId(),
                workspace.getPath(),
                files,
                workspace.getCreatedAt()
        );
    }

    private Path rootPath() {
        String configured = properties.getWorkspaces().getRootPath();
        String root = StringUtils.hasText(configured) ? configured : "workspaces";
        return Path.of(root).toAbsolutePath().normalize();
    }

    private String slug(FreelanceProject project) {
        String raw = "%s-%s".formatted(project.getPlatform(), project.getExternalId()).toLowerCase(Locale.ROOT);
        return raw.replaceAll("[^a-z0-9._-]+", "-").replaceAll("(^-|-$)", "");
    }
}
