package com.freelanceai.agent.analysis;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ProjectAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(ProjectAnalysisService.class);

    private final OpenAiProjectClassifier openAiProjectClassifier;
    private final HeuristicProjectClassifier heuristicProjectClassifier;

    public ProjectAnalysisService(
            OpenAiProjectClassifier openAiProjectClassifier,
            HeuristicProjectClassifier heuristicProjectClassifier
    ) {
        this.openAiProjectClassifier = openAiProjectClassifier;
        this.heuristicProjectClassifier = heuristicProjectClassifier;
    }

    public ProjectAnalysis analyze(String title, String description, BigDecimal price) {
        if (openAiProjectClassifier.isEnabled()) {
            try {
                return openAiProjectClassifier.classify(title, description, price);
            } catch (RuntimeException e) {
                log.warn("OpenAI classification failed; falling back to heuristic classifier", e);
            }
        }
        return heuristicProjectClassifier.classify(title, description, price);
    }
}
