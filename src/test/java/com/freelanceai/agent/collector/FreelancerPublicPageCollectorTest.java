package com.freelanceai.agent.collector;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import com.freelanceai.agent.config.FreelanceAiProperties;
import com.freelanceai.agent.project.ProjectPlatform;

class FreelancerPublicPageCollectorTest {

    private final FreelancerPublicPageCollector collector = new FreelancerPublicPageCollector(
            new FreelanceAiProperties(),
            RestClient.builder()
    );

    @Test
    void parsesFreelancerProjectCardsIntoCollectedProjects() {
        String html = """
                <html>
                  <body>
                    <div class="JobSearchCard-item-inner" data-project-card="true">
                      <a href="/projects/angular-js/fix-angular-asp-net-web"
                         class="JobSearchCard-primary-heading-link">
                        Fix Angular 20 + ASP.NET Web API 2 ERP Performance Issues
                      </a>
                      <p class="JobSearchCard-primary-description">
                        Diagnose frontend rendering and API latency problems.
                      </p>
                      <div class="JobSearchCard-primary-tags">
                        <a class="JobSearchCard-primary-tagsLink" href="/jobs/angular-js/">AngularJS</a>
                        <a class="JobSearchCard-primary-tagsLink" href="/jobs/asp-net/">ASP.NET</a>
                      </div>
                    </div>
                  </body>
                </html>
                """;

        assertThat(collector.parse("https://www.freelancer.com/jobs/", html))
                .singleElement()
                .satisfies(project -> {
                    assertThat(project.platform()).isEqualTo(ProjectPlatform.FREELANCER);
                    assertThat(project.externalId()).isEqualTo("projects-angular-js-fix-angular-asp-net-web");
                    assertThat(project.title()).isEqualTo("Fix Angular 20 + ASP.NET Web API 2 ERP Performance Issues");
                    assertThat(project.description()).isEqualTo("Diagnose frontend rendering and API latency problems.");
                    assertThat(project.sourceUrl()).isEqualTo("https://www.freelancer.com/projects/angular-js/fix-angular-asp-net-web");
                    assertThat(project.sourceCategory()).isEqualTo("AngularJS / ASP.NET");
                });
    }

    @Test
    void fallsBackToSeedPageWhenProjectCardsAreMissing() {
        String html = """
                <html>
                  <head>
                    <title>Freelance Jobs &amp; Contests | Find Work Today | Freelancer</title>
                    <meta name="description" content="Find the latest freelance jobs on Freelancer.com">
                  </head>
                  <body>
                    <h1>Freelance Jobs</h1>
                  </body>
                </html>
                """;

        assertThat(collector.parse("https://www.freelancer.com/jobs/", html))
                .singleElement()
                .satisfies(project -> {
                    assertThat(project.platform()).isEqualTo(ProjectPlatform.FREELANCER);
                    assertThat(project.externalId()).isEqualTo("jobs");
                    assertThat(project.title()).isEqualTo("Freelance Jobs");
                    assertThat(project.description()).isEqualTo("Find the latest freelance jobs on Freelancer.com");
                    assertThat(project.sourceUrl()).isEqualTo("https://www.freelancer.com/jobs/");
                    assertThat(project.sourceCategory()).isEqualTo("jobs");
                });
    }
}
