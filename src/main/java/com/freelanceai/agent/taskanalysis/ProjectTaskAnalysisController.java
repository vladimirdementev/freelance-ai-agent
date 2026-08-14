package com.freelanceai.agent.taskanalysis;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects/{projectId}/task-analysis")
public class ProjectTaskAnalysisController {

    private final ProjectTaskAnalysisService taskAnalysisService;

    public ProjectTaskAnalysisController(ProjectTaskAnalysisService taskAnalysisService) {
        this.taskAnalysisService = taskAnalysisService;
    }

    @PostMapping
    public TaskAnalysisResponse analyze(@PathVariable Long projectId) {
        return taskAnalysisService.analyze(projectId);
    }

    @GetMapping("/latest")
    public TaskAnalysisResponse latest(@PathVariable Long projectId) {
        return taskAnalysisService.latest(projectId);
    }
}
