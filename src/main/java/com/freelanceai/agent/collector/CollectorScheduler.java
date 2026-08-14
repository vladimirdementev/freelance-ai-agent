package com.freelanceai.agent.collector;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.freelanceai.agent.config.FreelanceAiProperties;

@Component
public class CollectorScheduler {

    private final FreelanceAiProperties properties;
    private final CollectorRunner collectorRunner;

    public CollectorScheduler(
            FreelanceAiProperties properties,
            CollectorRunner collectorRunner
    ) {
        this.properties = properties;
        this.collectorRunner = collectorRunner;
    }

    @Scheduled(fixedDelayString = "${freelance-ai.collectors.poll-interval-ms:1800000}")
    public void collectProjects() {
        if (!properties.getCollectors().isEnabled()) {
            return;
        }

        collectorRunner.runOnce();
    }
}
