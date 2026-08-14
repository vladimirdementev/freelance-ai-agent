package com.freelanceai.agent.analysis;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freelanceai.agent.config.FreelanceAiProperties;
import com.freelanceai.agent.project.ProjectCategory;
import com.freelanceai.agent.project.ProjectComplexity;

@Component
public class OpenAiProjectClassifier {

    private static final String ENDPOINT = "https://api.openai.com/v1/chat/completions";

    private final FreelanceAiProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public OpenAiProjectClassifier(FreelanceAiProperties properties, ObjectMapper objectMapper, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = restClientBuilder.build();
    }

    public boolean isEnabled() {
        return StringUtils.hasText(properties.getAi().getOpenaiApiKey());
    }

    public ProjectAnalysis classify(String title, String description, BigDecimal price) {
        Map<String, Object> request = Map.of(
                "model", properties.getAi().getOpenaiModel(),
                "temperature", 0.1,
                "response_format", Map.of("type", "json_object"),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt()),
                        Map.of("role", "user", "content", userPrompt(title, description, price))
                )
        );

        String response = restClient.post()
                .uri(ENDPOINT)
                .header("Authorization", "Bearer " + properties.getAi().getOpenaiApiKey())
                .body(request)
                .retrieve()
                .body(String.class);

        return parseResponse(response);
    }

    private String systemPrompt() {
        return """
                You classify Russian freelance orders for a developer focused on:
                Telegram bots, parsers, AI integrations, API integrations, and small backend systems.
                Return strict JSON with keys:
                category, complexity, technologies, estimatedHours, automationPercent, skillMatchPercent, riskPercent.
                category must be one of TELEGRAM_BOT, PARSER, AI_INTEGRATION, API_INTEGRATION, WEB_BACKEND, OTHER.
                complexity must be LOW, MEDIUM, HIGH, or UNKNOWN.
                technologies must be a short array of lowercase strings.
                Percent fields must be integers from 0 to 100.
                """;
    }

    private String userPrompt(String title, String description, BigDecimal price) {
        return """
                Title: %s
                Description: %s
                Price RUB: %s
                """.formatted(title, description, price == null ? "unknown" : price.toPlainString());
    }

    private ProjectAnalysis parseResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            String content = root.path("choices").path(0).path("message").path("content").asText();
            JsonNode analysis = objectMapper.readTree(content);
            return new ProjectAnalysis(
                    parseEnum(ProjectCategory.class, analysis.path("category").asText(), ProjectCategory.OTHER),
                    parseEnum(ProjectComplexity.class, analysis.path("complexity").asText(), ProjectComplexity.UNKNOWN),
                    parseTechnologies(analysis.path("technologies")),
                    clamp(analysis.path("estimatedHours").asInt(12)),
                    clamp(analysis.path("automationPercent").asInt(50)),
                    clamp(analysis.path("skillMatchPercent").asInt(50)),
                    clamp(analysis.path("riskPercent").asInt(50))
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse OpenAI classification response", e);
        }
    }

    private Set<String> parseTechnologies(JsonNode node) {
        List<String> values = new ArrayList<>();
        if (node.isArray()) {
            node.forEach(item -> values.add(item.asText()));
        }
        Set<String> technologies = new LinkedHashSet<>();
        for (String value : values) {
            String normalized = value.toLowerCase(Locale.ROOT).trim();
            if (StringUtils.hasText(normalized)) {
                technologies.add(normalized);
            }
        }
        return technologies;
    }

    private <E extends Enum<E>> E parseEnum(Class<E> type, String value, E fallback) {
        try {
            return Enum.valueOf(type, value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }
}
