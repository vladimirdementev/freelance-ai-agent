package com.freelanceai.agent.taskanalysis;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
public class ProjectTaskCommandController {

    private final ProjectTaskAnalysisService taskAnalysisService;

    public ProjectTaskCommandController(ProjectTaskAnalysisService taskAnalysisService) {
        this.taskAnalysisService = taskAnalysisService;
    }

    @PostMapping("/{projectId}/analyze-task")
    public TaskAnalysisResponse analyzeTask(@PathVariable Long projectId) {
        return taskAnalysisService.analyze(projectId);
    }
}
