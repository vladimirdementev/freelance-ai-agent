package com.freelanceai.agent.execution;

import java.time.Instant;

public record ExecutionRunResponse(
        Long id,
        Long projectId,
        Long workspaceId,
        ExecutionRunStatus status,
        String promptPath,
        String logsPath,
        String resultPath,
        String summary,
        Instant createdAt,
        Instant startedAt,
        Instant finishedAt
) {
}
