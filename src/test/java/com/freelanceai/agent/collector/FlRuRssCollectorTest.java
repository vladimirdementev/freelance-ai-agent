package com.freelanceai.agent.collector;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import com.freelanceai.agent.config.FreelanceAiProperties;
import com.freelanceai.agent.project.ProjectPlatform;

class FlRuRssCollectorTest {

    private final FlRuRssCollector collector = new FlRuRssCollector(
            new FreelanceAiProperties(),
            RestClient.builder()
    );

    @Test
    void parsesFlRuRssItemsIntoCollectedProjects() {
        String rss = """
                <?xml version="1.0" encoding="utf-8"?>
                <rss version="2.0">
                  <channel>
                    <item>
                      <title><![CDATA[Тестирование приложения (Бюджет: 50 000 ₽, для всех)]]></title>
                      <link>https://www.fl.ru/projects/5517886/testirovanie-prilojeniya.html</link>
                      <description><![CDATA[Требуется тестировщик мобильного приложения по готовому ТЗ]]></description>
                      <guid>https://www.fl.ru/projects/5517886/testirovanie-prilojeniya.html</guid>
                      <category><![CDATA[Разработка / Тестирование]]></category>
                      <pubDate>Fri, 14 Aug 2026 11:50:03 GMT</pubDate>
                    </item>
                  </channel>
                </rss>
                """;

        assertThat(collector.parse(rss))
                .singleElement()
                .satisfies(project -> {
                    assertThat(project.platform()).isEqualTo(ProjectPlatform.FL_RU);
                    assertThat(project.externalId()).isEqualTo("5517886");
                    assertThat(project.title()).isEqualTo("Тестирование приложения");
                    assertThat(project.description()).contains("Требуется тестировщик");
                    assertThat(project.price()).isEqualByComparingTo(BigDecimal.valueOf(50_000));
                    assertThat(project.publishedAt()).isEqualTo(Instant.parse("2026-08-14T11:50:03Z"));
                });
    }
}
