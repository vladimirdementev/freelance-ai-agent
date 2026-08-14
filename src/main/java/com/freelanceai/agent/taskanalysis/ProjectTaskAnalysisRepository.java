package com.freelanceai.agent.taskanalysis;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectTaskAnalysisRepository extends JpaRepository<ProjectTaskAnalysis, Long> {

    Optional<ProjectTaskAnalysis> findFirstByProjectIdOrderByCreatedAtDesc(Long projectId);
}
