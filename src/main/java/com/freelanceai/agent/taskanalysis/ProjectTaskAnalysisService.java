package com.freelanceai.agent.taskanalysis;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freelanceai.agent.project.FreelanceProject;
import com.freelanceai.agent.project.FreelanceProjectRepository;

@Service
public class ProjectTaskAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(ProjectTaskAnalysisService.class);
    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {
    };

    private final FreelanceProjectRepository projectRepository;
    private final ProjectTaskAnalysisRepository taskAnalysisRepository;
    private final OpenAiTaskAnalyzer openAiTaskAnalyzer;
    private final HeuristicTaskAnalyzer heuristicTaskAnalyzer;
    private final ObjectMapper objectMapper;

    public ProjectTaskAnalysisService(
            FreelanceProjectRepository projectRepository,
            ProjectTaskAnalysisRepository taskAnalysisRepository,
            OpenAiTaskAnalyzer openAiTaskAnalyzer,
            HeuristicTaskAnalyzer heuristicTaskAnalyzer,
            ObjectMapper objectMapper
    ) {
        this.projectRepository = projectRepository;
        this.taskAnalysisRepository = taskAnalysisRepository;
        this.openAiTaskAnalyzer = openAiTaskAnalyzer;
        this.heuristicTaskAnalyzer = heuristicTaskAnalyzer;
        this.objectMapper = objectMapper;
    }

    public TaskAnalysisResponse analyze(Long projectId) {
        FreelanceProject project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));

        TaskAnalysisResult result = analyzeProject(project);
        ProjectTaskAnalysis entity = new ProjectTaskAnalysis();
        entity.setProject(project);
        entity.setRequirements(toJson(result.requirements()));
        entity.setQuestions(toJson(result.questions()));
        entity.setRisks(toJson(result.risks()));
        entity.setImplementationPlan(toJson(result.implementationPlan()));
        entity.setAcceptanceCriteria(toJson(result.acceptanceCriteria()));
        entity.setAnalyzer(result.analyzer());

        return toResponse(taskAnalysisRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public TaskAnalysisResponse latest(Long projectId) {
        return taskAnalysisRepository.findFirstByProjectIdOrderByCreatedAtDesc(projectId)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Task analysis not found for project: " + projectId));
    }

    private TaskAnalysisResult analyzeProject(FreelanceProject project) {
        if (openAiTaskAnalyzer.isEnabled()) {
            try {
                return openAiTaskAnalyzer.analyze(project);
            } catch (RuntimeException e) {
                log.warn("OpenAI task analysis failed; falling back to heuristic analyzer", e);
            }
        }
        return heuristicTaskAnalyzer.analyze(project);
    }

    private TaskAnalysisResponse toResponse(ProjectTaskAnalysis entity) {
        return new TaskAnalysisResponse(
                entity.getId(),
                entity.getProject().getId(),
                fromJson(entity.getRequirements()),
                fromJson(entity.getQuestions()),
                fromJson(entity.getRisks()),
                fromJson(entity.getImplementationPlan()),
                fromJson(entity.getAcceptanceCriteria()),
                entity.getAnalyzer(),
                entity.getCreatedAt()
        );
    }

    private String toJson(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize task analysis", e);
        }
    }

    private List<String> fromJson(String value) {
        try {
            return objectMapper.readValue(value, STRING_LIST);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize task analysis", e);
        }
    }
}
