package com.freelanceai.agent.taskanalysis;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.freelanceai.agent.project.FreelanceProject;
import com.freelanceai.agent.project.ProjectCategory;

@Component
public class HeuristicTaskAnalyzer implements TaskAnalyzer {

    @Override
    public TaskAnalysisResult analyze(FreelanceProject project) {
        List<String> requirements = new ArrayList<>();
        requirements.add("Уточнить полное техническое задание и ожидаемый результат.");
        requirements.add("Зафиксировать входные данные, ограничения и формат сдачи результата.");
        requirements.add("Подготовить реализацию с инструкцией по запуску и проверке.");

        if (project.getCategory() == ProjectCategory.TELEGRAM_BOT) {
            requirements.add("Описать сценарии пользователя и администратора Telegram-бота.");
            requirements.add("Определить способ хранения данных и интеграции с внешними API.");
        } else if (project.getCategory() == ProjectCategory.PARSER) {
            requirements.add("Определить источники данных, частоту сбора и формат выгрузки.");
            requirements.add("Проверить ограничения сайтов-источников и требования к устойчивости парсинга.");
        } else if (project.getCategory() == ProjectCategory.AI_INTEGRATION) {
            requirements.add("Определить модель, prompt, формат ответа и критерии качества AI-результата.");
        }

        List<String> questions = List.of(
                "Какой точный критерий готовности результата?",
                "Есть ли доступы, API-ключи, тестовые аккаунты или примеры данных?",
                "Какие ограничения по срокам, бюджету и технологии являются жесткими?",
                "Нужны ли Docker, README, тесты и инструкция для самостоятельного запуска?"
        );

        List<String> risks = List.of(
                "В описании заказа могут отсутствовать важные детали, влияющие на оценку времени.",
                "Интеграции с внешними сервисами могут потребовать доступы или согласования.",
                "Цена заказа может не покрывать дополнительные требования, если они появятся после старта."
        );

        List<String> implementationPlan = List.of(
                "Согласовать недостающие вопросы и критерии приемки.",
                "Подготовить минимальную архитектуру решения.",
                "Создать рабочий проект или доработать существующий код.",
                "Реализовать основной сценарий.",
                "Добавить базовые проверки и обработку ошибок.",
                "Подготовить README, инструкцию запуска и список ограничений.",
                "Провести финальную проверку результата перед передачей."
        );

        List<String> acceptanceCriteria = List.of(
                "Решение запускается по инструкции без ручных скрытых шагов.",
                "Основной пользовательский сценарий работает на тестовых данных.",
                "Ошибки внешних сервисов или некорректные данные не ломают приложение без объяснения.",
                "Код, конфигурация и инструкция готовы к передаче заказчику."
        );

        return new TaskAnalysisResult(
                requirements,
                questions,
                risks,
                implementationPlan,
                acceptanceCriteria,
                "heuristic"
        );
    }
}
