package com.freelanceai.agent.taskanalysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freelanceai.agent.config.FreelanceAiProperties;
import com.freelanceai.agent.project.FreelanceProject;

@Component
public class OpenAiTaskAnalyzer implements TaskAnalyzer {

    private static final String ENDPOINT = "https://api.openai.com/v1/chat/completions";

    private final FreelanceAiProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public OpenAiTaskAnalyzer(FreelanceAiProperties properties, ObjectMapper objectMapper, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = restClientBuilder.build();
    }

    public boolean isEnabled() {
        return StringUtils.hasText(properties.getAi().getOpenaiApiKey());
    }

    @Override
    public TaskAnalysisResult analyze(FreelanceProject project) {
        Map<String, Object> request = Map.of(
                "model", properties.getAi().getOpenaiModel(),
                "temperature", 0.2,
                "response_format", Map.of("type", "json_object"),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt()),
                        Map.of("role", "user", "content", userPrompt(project))
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
                You analyze a freelance order before implementation.
                Return strict JSON with keys:
                requirements, questions, risks, implementationPlan, acceptanceCriteria.
                Every value must be an array of short Russian strings.
                Do not include markdown.
                Focus on making the task executable by a coding agent, but keep human approval required.
                """;
    }

    private String userPrompt(FreelanceProject project) {
        return """
                Platform: %s
                Source category: %s
                Title: %s
                Description: %s
                Price RUB: %s
                Detected category: %s
                Detected technologies: %s
                Detected complexity: %s
                Estimated hours: %s
                Risk percent: %s
                """.formatted(
                project.getPlatform(),
                project.getSourceCategory(),
                project.getTitle(),
                project.getDescription(),
                project.getPrice() == null ? "unknown" : project.getPrice().stripTrailingZeros().toPlainString(),
                project.getCategory(),
                project.getTechnologies(),
                project.getComplexity(),
                project.getEstimatedHours(),
                project.getRiskPercent()
        );
    }

    private TaskAnalysisResult parseResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            String content = root.path("choices").path(0).path("message").path("content").asText();
            JsonNode analysis = objectMapper.readTree(content);
            return new TaskAnalysisResult(
                    array(analysis, "requirements"),
                    array(analysis, "questions"),
                    array(analysis, "risks"),
                    array(analysis, "implementationPlan"),
                    array(analysis, "acceptanceCriteria"),
                    "openai"
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse OpenAI task analysis response", e);
        }
    }

    private List<String> array(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);
        if (!value.isArray()) {
            return List.of();
        }
        List<String> items = new ArrayList<>();
        value.forEach(item -> {
            String text = item.asText("").trim();
            if (StringUtils.hasText(text)) {
                items.add(text);
            }
        });
        return items;
    }
}
