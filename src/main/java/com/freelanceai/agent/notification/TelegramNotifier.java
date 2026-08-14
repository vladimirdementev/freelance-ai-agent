package com.freelanceai.agent.notification;

import java.math.BigDecimal;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import com.freelanceai.agent.config.FreelanceAiProperties;
import com.freelanceai.agent.project.FreelanceProject;

@Component
public class TelegramNotifier {

    private static final Logger log = LoggerFactory.getLogger(TelegramNotifier.class);

    private final FreelanceAiProperties properties;
    private final RestClient restClient;

    public TelegramNotifier(FreelanceAiProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClient = restClientBuilder.build();
    }

    public void notifyIfTopProject(FreelanceProject project) {
        if (!isConfigured() || project.getScore() == null) {
            return;
        }
        BigDecimal threshold = BigDecimal.valueOf(properties.getScoring().getMinNotificationScore());
        if (project.getScore().compareTo(threshold) < 0) {
            return;
        }

        try {
            restClient.post()
                    .uri("https://api.telegram.org/bot{token}/sendMessage", properties.getTelegram().getBotToken())
                    .body(Map.of(
                            "chat_id", properties.getTelegram().getChatId(),
                            "parse_mode", "HTML",
                            "text", format(project)
                    ))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RuntimeException e) {
            log.warn("Failed to send Telegram notification for project {}", project.getExternalId(), e);
        }
    }

    private boolean isConfigured() {
        return StringUtils.hasText(properties.getTelegram().getBotToken())
                && StringUtils.hasText(properties.getTelegram().getChatId());
    }

    private String format(FreelanceProject project) {
        return """
                <b>New matching freelance project</b>

                <b>%s</b>
                Platform: %s
                Price: %s RUB
                Score: %s

                Category: %s
                Complexity: %s
                Estimated time: %s h
                Automation: %s%%
                Skill match: %s%%
                Risk: %s%%

                Technologies: %s
                """.formatted(
                project.getTitle(),
                project.getPlatform(),
                project.getPrice() == null ? "not specified" : project.getPrice().stripTrailingZeros().toPlainString(),
                project.getScore(),
                project.getCategory(),
                project.getComplexity(),
                project.getEstimatedHours(),
                project.getAutomationPercent(),
                project.getSkillMatchPercent(),
                project.getRiskPercent(),
                project.getTechnologies().isEmpty() ? "-" : String.join(", ", project.getTechnologies())
        );
    }
}
