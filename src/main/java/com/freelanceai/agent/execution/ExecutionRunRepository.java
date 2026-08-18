package com.freelanceai.agent.execution;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ExecutionRunRepository extends JpaRepository<ExecutionRun, Long> {

    Optional<ExecutionRun> findFirstByProjectIdOrderByCreatedAtDesc(Long projectId);

    Optional<ExecutionRun> findByIdAndProjectId(Long id, Long projectId);
}
