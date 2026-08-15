package com.freelanceai.agent.workspace;

import java.time.Instant;
import java.util.List;

public record ProjectWorkspaceResponse(
        Long id,
        Long projectId,
        Long taskAnalysisId,
        String path,
        List<String> files,
        Instant createdAt
) {
}
