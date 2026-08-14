package com.freelanceai.agent.project;

import java.time.Instant;
import java.util.LinkedHashSet;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.freelanceai.agent.analysis.ProjectAnalysis;
import com.freelanceai.agent.analysis.ProjectAnalysisService;
import com.freelanceai.agent.notification.TelegramNotifier;
import com.freelanceai.agent.scoring.ScoringService;

@Service
public class ProjectIngestionService {

    private final FreelanceProjectRepository projectRepository;
    private final ProjectAnalysisService analysisService;
    private final ScoringService scoringService;
    private final TelegramNotifier telegramNotifier;

    public ProjectIngestionService(
            FreelanceProjectRepository projectRepository,
            ProjectAnalysisService analysisService,
            ScoringService scoringService,
            TelegramNotifier telegramNotifier
    ) {
        this.projectRepository = projectRepository;
        this.analysisService = analysisService;
        this.scoringService = scoringService;
        this.telegramNotifier = telegramNotifier;
    }

    @Transactional
    public FreelanceProject ingest(ProjectIngestRequest request) {
        FreelanceProject project = projectRepository
                .findByPlatformAndExternalId(request.platform(), request.externalId())
                .orElseGet(FreelanceProject::new);

        project.setPlatform(request.platform());
        project.setExternalId(request.externalId());
        project.setTitle(request.title());
        project.setDescription(request.description());
        project.setPrice(request.price());
        project.setPublishedAt(request.publishedAt() == null ? Instant.now() : request.publishedAt());

        ProjectAnalysis analysis = analysisService.analyze(request.title(), request.description(), request.price());
        project.setCategory(analysis.category());
        project.setComplexity(analysis.complexity());
        project.setTechnologies(new LinkedHashSet<>(analysis.technologies()));
        project.setEstimatedHours(analysis.estimatedHours());
        project.setAutomationPercent(analysis.automationPercent());
        project.setSkillMatchPercent(analysis.skillMatchPercent());
        project.setRiskPercent(analysis.riskPercent());
        project.setScore(scoringService.score(analysis, request.price()));

        FreelanceProject saved = projectRepository.save(project);
        telegramNotifier.notifyIfTopProject(saved);
        return saved;
    }
}
