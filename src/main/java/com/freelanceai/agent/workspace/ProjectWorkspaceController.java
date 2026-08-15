package com.freelanceai.agent.workspace;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects/{projectId}/workspace")
public class ProjectWorkspaceController {

    private final ProjectWorkspaceService workspaceService;

    public ProjectWorkspaceController(ProjectWorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @PostMapping
    public ProjectWorkspaceResponse createWorkspace(@PathVariable Long projectId) {
        return workspaceService.createWorkspace(projectId);
    }

    @GetMapping("/latest")
    public ProjectWorkspaceResponse latest(@PathVariable Long projectId) {
        return workspaceService.latest(projectId);
    }
}
