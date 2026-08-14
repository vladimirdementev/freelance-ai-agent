package com.freelanceai.agent.collector;

public record CollectorRunResult(
        int collected,
        int ingested,
        int failed
) {
}
