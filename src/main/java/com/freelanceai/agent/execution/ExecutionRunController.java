package com.freelanceai.agent.execution;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects/{projectId}/execution-runs")
public class ExecutionRunController {

    private final ExecutionRunService executionRunService;

    public ExecutionRunController(ExecutionRunService executionRunService) {
        this.executionRunService = executionRunService;
    }

    @PostMapping
    public ExecutionRunResponse createRun(@PathVariable Long projectId) {
        return executionRunService.createRun(projectId);
    }

    @GetMapping("/latest")
    public ExecutionRunResponse latest(@PathVariable Long projectId) {
        return executionRunService.latest(projectId);
    }
}
