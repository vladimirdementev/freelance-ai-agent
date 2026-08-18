package com.freelanceai.agent.collector;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import com.freelanceai.agent.config.FreelanceAiProperties;
import com.freelanceai.agent.project.ProjectPlatform;

class WorkzillaPublicPageCollectorTest {

    private final WorkzillaPublicPageCollector collector = new WorkzillaPublicPageCollector(
            new FreelanceAiProperties(),
            RestClient.builder()
    );

    @Test
    void parsesWorkzillaPublicPageIntoCollectedProject() {
        String html = """
                <html>
                  <head>
                    <title>Фриланс-работа по API — Workzilla</title>
                    <meta name="description" content="Проекты по API и интеграциям на Workzilla">
                  </head>
                  <body>
                    <h1>Фриланс-работа по API</h1>
                  </body>
                </html>
                """;

        assertThat(collector.parse("https://work-zilla.com/freelance-jobs/development-and-it/api-integrations", html))
                .singleElement()
                .satisfies(project -> {
                    assertThat(project.platform()).isEqualTo(ProjectPlatform.WORKZILLA);
                    assertThat(project.externalId()).isEqualTo("freelance-jobs-development-and-it-api-integrations");
                    assertThat(project.title()).isEqualTo("Фриланс-работа по API");
                    assertThat(project.description()).isEqualTo("Проекты по API и интеграциям на Workzilla");
                    assertThat(project.sourceUrl()).isEqualTo("https://work-zilla.com/freelance-jobs/development-and-it/api-integrations");
                    assertThat(project.sourceCategory()).isEqualTo("development and it / api integrations");
                });
    }
}
