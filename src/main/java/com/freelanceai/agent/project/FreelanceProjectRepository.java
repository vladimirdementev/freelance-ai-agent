package com.freelanceai.agent.project;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FreelanceProjectRepository extends JpaRepository<FreelanceProject, Long> {

    Optional<FreelanceProject> findByPlatformAndExternalId(ProjectPlatform platform, String externalId);

    List<FreelanceProject> findAllByOrderByScoreDesc(Pageable pageable);
}
