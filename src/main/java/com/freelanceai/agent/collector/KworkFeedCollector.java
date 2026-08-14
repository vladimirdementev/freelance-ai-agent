package com.freelanceai.agent.collector;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freelanceai.agent.config.FreelanceAiProperties;
import com.freelanceai.agent.project.ProjectPlatform;

@Component
public class KworkFeedCollector implements ProjectCollector {

    private static final Logger log = LoggerFactory.getLogger(KworkFeedCollector.class);

    private final FreelanceAiProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public KworkFeedCollector(FreelanceAiProperties properties, ObjectMapper objectMapper, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = restClientBuilder.build();
    }

    @Override
    public List<CollectedProject> collect() {
        String feedUrl = properties.getCollectors().getKworkFeedUrl();
        if (!StringUtils.hasText(feedUrl)) {
            return List.of();
        }

        try {
            String response = restClient.get()
                    .uri(feedUrl)
                    .retrieve()
                    .body(String.class);
            return parse(response);
        } catch (RuntimeException e) {
            log.warn("Kwork feed collection failed", e);
            return List.of();
        }
    }

    private List<CollectedProject> parse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode items = root.isArray() ? root : root.path("items");
            if (!items.isArray()) {
                return List.of();
            }

            List<CollectedProject> projects = new ArrayList<>();
            for (JsonNode item : items) {
                projects.add(new CollectedProject(
                        ProjectPlatform.KWORK,
                        item.path("externalId").asText(item.path("id").asText()),
                        item.path("title").asText(),
                        item.path("description").asText(),
                        parsePrice(item.path("price")),
                        parseInstant(item.path("publishedAt").asText(null))
                ));
            }
            return projects;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse Kwork feed", e);
        }
    }

    private BigDecimal parsePrice(JsonNode node) {
        if (node.isMissingNode() || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            return node.decimalValue();
        }
        String value = node.asText("").replace(" ", "");
        return StringUtils.hasText(value) ? new BigDecimal(value) : null;
    }

    private Instant parseInstant(String value) {
        return StringUtils.hasText(value) ? Instant.parse(value) : Instant.now();
    }
}
