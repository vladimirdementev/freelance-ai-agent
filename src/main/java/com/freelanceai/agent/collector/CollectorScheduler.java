package com.freelanceai.agent.collector;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.freelanceai.agent.config.FreelanceAiProperties;
import com.freelanceai.agent.project.ProjectIngestionService;

@Component
public class CollectorScheduler {

    private static final Logger log = LoggerFactory.getLogger(CollectorScheduler.class);

    private final FreelanceAiProperties properties;
    private final List<ProjectCollector> collectors;
    private final ProjectIngestionService ingestionService;

    public CollectorScheduler(
            FreelanceAiProperties properties,
            List<ProjectCollector> collectors,
            ProjectIngestionService ingestionService
    ) {
        this.properties = properties;
        this.collectors = collectors;
        this.ingestionService = ingestionService;
    }

    @Scheduled(fixedDelayString = "${freelance-ai.collectors.poll-interval-ms:1800000}")
    public void collectProjects() {
        if (!properties.getCollectors().isEnabled()) {
            return;
        }

        for (ProjectCollector collector : collectors) {
            collector.collect().forEach(project -> {
                try {
                    ingestionService.ingest(project.toIngestRequest());
                } catch (RuntimeException e) {
                    log.warn("Failed to ingest collected project {}", project.externalId(), e);
                }
            });
        }
    }
}
