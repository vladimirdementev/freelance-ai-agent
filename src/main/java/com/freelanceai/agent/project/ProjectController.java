package com.freelanceai.agent.project;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectIngestionService ingestionService;
    private final FreelanceProjectRepository projectRepository;

    public ProjectController(ProjectIngestionService ingestionService, FreelanceProjectRepository projectRepository) {
        this.ingestionService = ingestionService;
        this.projectRepository = projectRepository;
    }

    @PostMapping("/ingest")
    public ProjectResponse ingest(@Valid @RequestBody ProjectIngestRequest request) {
        return ProjectResponse.from(ingestionService.ingest(request));
    }

    @GetMapping("/top")
    public List<ProjectResponse> topProjects(
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit
    ) {
        return projectRepository.findAllByOrderByScoreDesc(PageRequest.of(0, limit))
                .stream()
                .map(ProjectResponse::from)
                .toList();
    }
}
