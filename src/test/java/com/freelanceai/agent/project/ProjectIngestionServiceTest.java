package com.freelanceai.agent.project;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.freelanceai.agent.analysis.ProjectAnalysis;
import com.freelanceai.agent.analysis.ProjectAnalysisService;
import com.freelanceai.agent.notification.TelegramNotifier;
import com.freelanceai.agent.scoring.ScoringService;

@ExtendWith(MockitoExtension.class)
class ProjectIngestionServiceTest {

    @Mock
    private FreelanceProjectRepository projectRepository;

    @Mock
    private ProjectAnalysisService analysisService;

    @Mock
    private ScoringService scoringService;

    @Mock
    private TelegramNotifier telegramNotifier;

    @InjectMocks
    private ProjectIngestionService ingestionService;

    @Test
    void doesNotSendDuplicateTelegramNotificationForAlreadyNotifiedProject() {
        FreelanceProject existing = new FreelanceProject();
        existing.setPlatform(ProjectPlatform.FREELANCER);
        existing.setExternalId("projects-api-integrations-telegram-bot");
        existing.setNotifiedAt(Instant.parse("2026-08-14T11:50:03Z"));

        ProjectIngestRequest request = new ProjectIngestRequest(
                ProjectPlatform.FREELANCER,
                "projects-api-integrations-telegram-bot",
                "Telegram bot",
                "Need a Telegram bot",
                BigDecimal.valueOf(10_000),
                Instant.parse("2026-08-14T11:40:00Z"),
                "https://www.freelancer.com/projects/api-integrations/telegram-bot",
                "Разработка / Чат-боты"
        );
        ProjectAnalysis analysis = new ProjectAnalysis(
                ProjectCategory.TELEGRAM_BOT,
                ProjectComplexity.MEDIUM,
                Set.of("telegram"),
                8,
                85,
                90,
                20
        );

        when(projectRepository.findByPlatformAndExternalId(
                ProjectPlatform.FREELANCER,
                "projects-api-integrations-telegram-bot"
        )).thenReturn(Optional.of(existing));
        when(analysisService.analyze(request.title(), request.description(), request.price())).thenReturn(analysis);
        when(scoringService.score(analysis, request.price())).thenReturn(BigDecimal.valueOf(90));
        when(projectRepository.save(existing)).thenReturn(existing);

        ingestionService.ingest(request);

        verify(telegramNotifier, never()).notifyIfTopProject(any());
    }
}
