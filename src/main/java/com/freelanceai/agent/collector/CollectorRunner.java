package com.freelanceai.agent.collector;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.freelanceai.agent.project.ProjectIngestionService;

@Service
public class CollectorRunner {

    private static final Logger log = LoggerFactory.getLogger(CollectorRunner.class);

    private final List<ProjectCollector> collectors;
    private final ProjectIngestionService ingestionService;

    public CollectorRunner(List<ProjectCollector> collectors, ProjectIngestionService ingestionService) {
        this.collectors = collectors;
        this.ingestionService = ingestionService;
    }

    public CollectorRunResult runOnce() {
        int collected = 0;
        int ingested = 0;
        int failed = 0;

        for (ProjectCollector collector : collectors) {
            List<CollectedProject> projects = collector.collect();
            collected += projects.size();

            for (CollectedProject project : projects) {
                try {
                    ingestionService.ingest(project.toIngestRequest());
                    ingested++;
                } catch (RuntimeException e) {
                    failed++;
                    log.warn("Failed to ingest collected project {}", project.externalId(), e);
                }
            }
        }

        return new CollectorRunResult(collected, ingested, failed);
    }
}
