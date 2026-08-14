package com.freelanceai.agent.collector;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/collectors")
public class CollectorController {

    private final CollectorRunner collectorRunner;

    public CollectorController(CollectorRunner collectorRunner) {
        this.collectorRunner = collectorRunner;
    }

    @PostMapping("/run")
    public CollectorRunResult runCollectors() {
        return collectorRunner.runOnce();
    }
}
