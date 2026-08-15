package com.freelanceai.agent.workspace;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectWorkspaceRepository extends JpaRepository<ProjectWorkspace, Long> {

    Optional<ProjectWorkspace> findFirstByProjectIdOrderByCreatedAtDesc(Long projectId);
}
