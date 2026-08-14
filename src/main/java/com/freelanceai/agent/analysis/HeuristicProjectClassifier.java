package com.freelanceai.agent.analysis;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.freelanceai.agent.project.ProjectCategory;
import com.freelanceai.agent.project.ProjectComplexity;

@Component
public class HeuristicProjectClassifier implements ProjectClassifier {

    @Override
    public ProjectAnalysis classify(String title, String description, BigDecimal price) {
        String text = ((title == null ? "" : title) + " " + (description == null ? "" : description)).toLowerCase(Locale.ROOT);
        ProjectCategory category = detectCategory(text);
        Set<String> technologies = detectTechnologies(text, category);
        ProjectComplexity complexity = detectComplexity(text);
        int estimatedHours = estimateHours(category, complexity, text);
        int automationPercent = estimateAutomation(category, complexity);
        int skillMatchPercent = estimateSkillMatch(category, technologies);
        int riskPercent = estimateRisk(complexity, text);

        return new ProjectAnalysis(
                category,
                complexity,
                technologies,
                estimatedHours,
                automationPercent,
                skillMatchPercent,
                riskPercent
        );
    }

    private ProjectCategory detectCategory(String text) {
        if (containsAny(text, "telegram", "телеграм", "bot", "бот")) {
            return ProjectCategory.TELEGRAM_BOT;
        }
        if (containsAny(text, "parser", "парсер", "scraping", "скрап", "собрать данные")) {
            return ProjectCategory.PARSER;
        }
        if (containsAny(text, "ai", "openai", "llm", "gpt", "нейросет", "искусствен")) {
            return ProjectCategory.AI_INTEGRATION;
        }
        if (containsAny(text, "api", "интеграц", "webhook", "вебхук")) {
            return ProjectCategory.API_INTEGRATION;
        }
        if (containsAny(text, "backend", "spring", "database", "postgres", "сервер")) {
            return ProjectCategory.WEB_BACKEND;
        }
        return ProjectCategory.OTHER;
    }

    private Set<String> detectTechnologies(String text, ProjectCategory category) {
        Set<String> technologies = new LinkedHashSet<>();
        if (category == ProjectCategory.TELEGRAM_BOT) {
            technologies.add("telegram");
        }
        if (category == ProjectCategory.PARSER) {
            technologies.add("parser");
        }
        if (category == ProjectCategory.AI_INTEGRATION) {
            technologies.add("llm");
        }
        addIfContains(technologies, text, "java", "java");
        addIfContains(technologies, text, "spring", "spring");
        addIfContains(technologies, text, "postgresql", "postgres", "postgresql");
        addIfContains(technologies, text, "python", "python", "питон");
        addIfContains(technologies, text, "react", "react");
        addIfContains(technologies, text, "payments", "payment", "оплат", "платеж");
        addIfContains(technologies, text, "api", "api", "webhook", "вебхук");
        return technologies;
    }

    private ProjectComplexity detectComplexity(String text) {
        if (containsAny(text, "с нуля", "архитект", "личный кабинет", "админ", "несколько источников", "rag", "vector")) {
            return ProjectComplexity.HIGH;
        }
        if (containsAny(text, "оплат", "интеграц", "api", "база данных", "postgres", "уведомлен")) {
            return ProjectComplexity.MEDIUM;
        }
        if (containsAny(text, "простой", "небольшой", "лендинг", "правка", "доработать")) {
            return ProjectComplexity.LOW;
        }
        return ProjectComplexity.MEDIUM;
    }

    private int estimateHours(ProjectCategory category, ProjectComplexity complexity, String text) {
        int base = switch (category) {
            case TELEGRAM_BOT -> 10;
            case PARSER -> 8;
            case AI_INTEGRATION -> 14;
            case API_INTEGRATION -> 8;
            case WEB_BACKEND -> 16;
            case OTHER -> 12;
        };
        int multiplier = switch (complexity) {
            case LOW -> 1;
            case MEDIUM, UNKNOWN -> 2;
            case HIGH -> 4;
        };
        int integrationsPenalty = containsAny(text, "оплат", "payment", "crm", "amo", "bitrix", "1c", "1с") ? 4 : 0;
        return Math.min(80, base * multiplier / 2 + integrationsPenalty);
    }

    private int estimateAutomation(ProjectCategory category, ProjectComplexity complexity) {
        int base = switch (category) {
            case TELEGRAM_BOT -> 85;
            case PARSER -> 80;
            case AI_INTEGRATION -> 75;
            case API_INTEGRATION -> 70;
            case WEB_BACKEND -> 55;
            case OTHER -> 35;
        };
        int penalty = complexity == ProjectComplexity.HIGH ? 15 : 0;
        return clamp(base - penalty);
    }

    private int estimateSkillMatch(ProjectCategory category, Set<String> technologies) {
        int base = switch (category) {
            case TELEGRAM_BOT, PARSER, AI_INTEGRATION -> 88;
            case API_INTEGRATION -> 78;
            case WEB_BACKEND -> 72;
            case OTHER -> 45;
        };
        int bonus = technologies.contains("spring") || technologies.contains("postgresql") ? 5 : 0;
        return clamp(base + bonus);
    }

    private int estimateRisk(ProjectComplexity complexity, String text) {
        int base = switch (complexity) {
            case LOW -> 15;
            case MEDIUM, UNKNOWN -> 35;
            case HIGH -> 60;
        };
        int vaguePenalty = containsAny(text, "срочно", "любой ценой", "дешево", "как можно быстрее") ? 15 : 0;
        return clamp(base + vaguePenalty);
    }

    private void addIfContains(Set<String> technologies, String text, String technology, String... needles) {
        if (containsAny(text, needles)) {
            technologies.add(technology);
        }
    }

    private boolean containsAny(String text, String... needles) {
        for (String needle : needles) {
            if (text.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }
}
