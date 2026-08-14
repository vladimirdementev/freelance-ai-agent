package com.freelanceai.agent.notification;

import java.math.BigDecimal;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;
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

    public boolean notifyIfTopProject(FreelanceProject project) {
        if (!isConfigured() || project.getScore() == null) {
            return false;
        }
        BigDecimal threshold = BigDecimal.valueOf(properties.getScoring().getMinNotificationScore());
        if (project.getScore().compareTo(threshold) < 0) {
            return false;
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
            return true;
        } catch (RuntimeException e) {
            log.warn("Failed to send Telegram notification for project {}", project.getExternalId(), e);
            return false;
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
                Source category: %s
                Price: %s RUB
                Score: %s

                Category: %s
                Complexity: %s
                Estimated time: %s h
                Automation: %s%%
                Skill match: %s%%
                Risk: %s%%

                Technologies: %s
                %s
                """.formatted(
                escape(project.getTitle()),
                project.getPlatform(),
                escape(project.getSourceCategory() == null ? "-" : project.getSourceCategory()),
                project.getPrice() == null ? "not specified" : project.getPrice().stripTrailingZeros().toPlainString(),
                project.getScore(),
                project.getCategory(),
                project.getComplexity(),
                project.getEstimatedHours(),
                project.getAutomationPercent(),
                project.getSkillMatchPercent(),
                project.getRiskPercent(),
                escape(project.getTechnologies().isEmpty() ? "-" : String.join(", ", project.getTechnologies())),
                sourceLink(project)
        );
    }

    private String sourceLink(FreelanceProject project) {
        if (!StringUtils.hasText(project.getSourceUrl())) {
            return "";
        }
        return "\n<a href=\"%s\">Open project</a>".formatted(escape(project.getSourceUrl()));
    }

    private String escape(String value) {
        return HtmlUtils.htmlEscape(value);
    }
}
