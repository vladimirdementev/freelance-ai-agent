package com.freelanceai.agent.analysis;

import java.math.BigDecimal;

public interface ProjectClassifier {

    ProjectAnalysis classify(String title, String description, BigDecimal price);
}
