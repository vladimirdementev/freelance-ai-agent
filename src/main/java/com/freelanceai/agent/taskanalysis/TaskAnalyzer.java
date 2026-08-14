package com.freelanceai.agent.taskanalysis;

import com.freelanceai.agent.project.FreelanceProject;

public interface TaskAnalyzer {

    TaskAnalysisResult analyze(FreelanceProject project);
}
