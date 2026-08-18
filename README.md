# Freelance AI Agent

MVP backend для поиска фриланс-заказов, их классификации, расчета привлекательности и отправки лучших вариантов в Telegram.

Первая версия специально сделана узкой. Она умеет:

1. получать данные из публичных страниц Workzilla или другого парсерного слоя;
2. приводить заказ к единому формату;
3. сохранять заказы в PostgreSQL;
4. классифицировать заказ через OpenAI, если задан API-ключ;
5. использовать локальный эвристический классификатор, если OpenAI не настроен;
6. рассчитывать score заказа;
7. отправлять Telegram-уведомление, если заказ проходит заданный порог;
8. не отправлять повторные уведомления по уже уведомленным заказам;
9. разбирать выбранный заказ в требования, вопросы, риски, план реализации и критерии приемки;
10. создавать execution workspace с markdown-файлами для дальнейшей реализации заказа;
11. создавать execution run и prompt для coding agent.

В MVP пока нет автоматической отправки откликов, CRM, мобильного приложения и AI coding agent. Сначала нужно проверить, что система стабильно находит выгодные заказы.

## Архитектура

```text
Workzilla public pages / Kwork feed / parser
  -> /api/projects/ingest
  -> PostgreSQL
  -> AI / heuristic classifier
  -> scoring engine
  -> top projects API
  -> Telegram notification
```

## Технологии

- Java 21
- Spring Boot 4.1
- PostgreSQL
- Flyway
- Maven
- Docker Compose

## Локальный запуск

```bash
docker compose up --build
```

Опциональные переменные окружения:

| Переменная | Назначение | Значение по умолчанию |
| --- | --- | --- |
| `OPENAI_API_KEY` | Включает классификацию через OpenAI | пусто |
| `OPENAI_MODEL` | Модель OpenAI | `gpt-4.1-mini` |
| `TELEGRAM_BOT_TOKEN` | Токен Telegram-бота | пусто |
| `TELEGRAM_CHAT_ID` | ID Telegram-чата для уведомлений | пусто |
| `TARGET_HOURLY_RATE` | Целевая ставка в рублях в час для score | `1500` |
| `MIN_NOTIFICATION_SCORE` | Минимальный score для Telegram-уведомления | `80` |
| `COLLECTORS_ENABLED` | Включает scheduled collectors | `false` |
| `KWORK_FEED_URL` | URL нормализованного JSON-фида Kwork | пусто |
| `WORKZILLA_SEED_URLS` | Список публичных страниц Workzilla для collector | IT/category URLs |
| `WORKSPACES_ROOT` | Папка для execution workspaces | `workspaces` |
| `EXECUTION_AGENT_COMMAND` | Команда, которую worker запускает для выполнения workspace | пусто |
| `EXECUTION_TIMEOUT_SECONDS` | Timeout запуска execution agent | `1800` |

## API

### Загрузить один нормализованный заказ

```bash
curl -X POST http://localhost:8080/api/projects/ingest \
  -H 'Content-Type: application/json' \
  -d '{
    "platform": "KWORK",
    "externalId": "kwork-123",
    "title": "Telegram bot with payments",
    "description": "Need a Telegram bot with catalog, PostgreSQL, payment API and admin notifications.",
    "price": 18000,
    "publishedAt": "2026-08-14T10:00:00Z",
    "sourceUrl": "https://example.com/orders/kwork-123",
    "sourceCategory": "Telegram bots"
  }'
```

### Получить топ заказов

```bash
curl 'http://localhost:8080/api/projects/top?limit=10'
```

### Проанализировать ТЗ выбранного заказа

```bash
curl -X POST http://localhost:8080/api/projects/1/analyze-task
```

Альтернативный resource endpoint:

```bash
curl -X POST http://localhost:8080/api/projects/1/task-analysis
```

Получить последний сохраненный анализ:

```bash
curl http://localhost:8080/api/projects/1/task-analysis/latest
```

Пример ответа:

```json
{
  "id": 10,
  "projectId": 1,
  "requirements": [
    "Уточнить полное техническое задание и ожидаемый результат"
  ],
  "questions": [
    "Есть ли доступы, API-ключи, тестовые аккаунты или примеры данных?"
  ],
  "risks": [
    "В описании заказа могут отсутствовать важные детали"
  ],
  "implementationPlan": [
    "Согласовать недостающие вопросы и критерии приемки",
    "Подготовить минимальную архитектуру решения",
    "Реализовать основной сценарий"
  ],
  "acceptanceCriteria": [
    "Решение запускается по инструкции без ручных скрытых шагов"
  ],
  "analyzer": "heuristic",
  "createdAt": "2026-08-14T12:00:00Z"
}
```

### Создать execution workspace для заказа

```bash
curl -X POST http://localhost:8080/api/projects/1/workspace
```

Если для заказа ещё нет task analysis, система сначала создаст его автоматически.

Ответ:

```json
{
  "id": 3,
  "projectId": 1,
  "taskAnalysisId": 10,
  "path": "/app/workspaces/workzilla-freelance-jobs-development-and-it-api-integrations",
  "files": [
    "task.md",
    "requirements.md",
    "questions.md",
    "risks.md",
    "implementation-plan.md",
    "acceptance-criteria.md",
    "README.md"
  ],
  "createdAt": "2026-08-14T12:10:00Z"
}
```

Получить последний созданный workspace:

```bash
curl http://localhost:8080/api/projects/1/workspace/latest
```

### Создать execution run для coding agent

```bash
curl -X POST http://localhost:8080/api/projects/1/execution-runs
```

Если workspace ещё нет, система создаст его автоматически.

Ответ:

```json
{
  "id": 5,
  "projectId": 1,
  "workspaceId": 3,
  "status": "READY_FOR_AGENT",
  "promptPath": "/app/workspaces/workzilla-freelance-jobs-development-and-it-api-integrations/execution-prompt.md",
  "logsPath": "/app/workspaces/workzilla-freelance-jobs-development-and-it-api-integrations/execution.log",
  "resultPath": "/app/workspaces/workzilla-freelance-jobs-development-and-it-api-integrations/implementation",
  "summary": "Execution prompt is ready for a coding agent.",
  "createdAt": "2026-08-15T10:20:00Z",
  "startedAt": null,
  "finishedAt": null
}
```

Получить последний execution run:

```bash
curl http://localhost:8080/api/projects/1/execution-runs/latest
```

Запустить execution run:

```bash
curl -X POST http://localhost:8080/api/projects/1/execution-runs/5/start
```

Запуск работает только если задана переменная `EXECUTION_AGENT_COMMAND`. Команда берется из конфигурации приложения, а не из API.

### Запустить collectors вручную

```bash
curl -X POST http://localhost:8080/api/collectors/run
```

Ответ показывает, сколько заказов найдено, сохранено и сколько упало при обработке:

```json
{
  "collected": 25,
  "ingested": 25,
  "failed": 0
}
```

## Workzilla collector

У Workzilla не обнаружен публичный RSS/API с актуальными заданиями без авторизации. Поэтому текущий collector использует список публичных страниц Workzilla как seed URLs:

```text
https://work-zilla.com/freelance-jobs/development-and-it
https://work-zilla.com/freelance-jobs/development-and-it/api-integrations
https://work-zilla.com/freelance-jobs/development-and-it/chatbot-development/development-of-telegram-bot-parser
https://work-zilla.com/freelance-jobs/development-and-it/parsing/development-of-telegram-parser
```

Collector читает HTML страницы, извлекает `h1`/`title`, meta description, source URL и source category, затем передает запись в общий pipeline:

```text
Workzilla public page
  -> CollectedProject
  -> ProjectIngestRequest
  -> PostgreSQL
  -> classifier
  -> scoring
  -> Telegram
```

Чтобы включить scheduled collection:

```bash
COLLECTORS_ENABLED=true docker compose up --build
```

Повторные уведомления не отправляются: после успешной отправки Telegram-сообщения у заказа заполняется `notifiedAt`.

Важно: текущий Workzilla collector — это публичный адаптер без авторизации. Для реальных актуальных заданий Workzilla может потребоваться авторизованный API/mobile adapter или отдельный экспорт задач.

## AI task analysis

Task analysis — это первый шаг к автоматическому выполнению заказа.

Он берет уже найденный и сохраненный заказ и формирует:

- требования;
- вопросы к заказчику;
- риски;
- план реализации;
- критерии приемки.

Если задан `OPENAI_API_KEY`, анализ делает OpenAI. Если ключ не задан или OpenAI вернул ошибку, используется локальный heuristic analyzer.

## Execution workspace

Execution workspace — это папка с подготовленным пакетом задачи для реализации.

Структура:

```text
workspaces/
  workzilla-freelance-jobs-development-and-it-api-integrations/
    task.md
    requirements.md
    questions.md
    risks.md
    implementation-plan.md
    acceptance-criteria.md
    execution-prompt.md
    execution.log
    implementation/
    README.md
```

Назначение файлов:

- `task.md` — исходный заказ, ссылка, цена, score и detected metadata;
- `requirements.md` — требования;
- `questions.md` — вопросы, которые нужно уточнить перед работой;
- `risks.md` — риски;
- `implementation-plan.md` — план реализации;
- `acceptance-criteria.md` — критерии готовности;
- `execution-prompt.md` — инструкция для coding agent;
- `implementation/` — папка, куда coding agent должен класть реализацию;
- `README.md` — инструкция по использованию workspace.

Следующий шаг после этого — подключить coding agent, который будет читать workspace и создавать реализацию в отдельной папке или репозитории.

## Execution agent runner

Execution runner запускает настроенную команду в папке workspace.

Пример конфигурации:

```bash
EXECUTION_AGENT_COMMAND='your-agent-command --prompt "$EXECUTION_PROMPT_PATH" --output "$EXECUTION_RESULT_PATH"'
```

Процесс получает переменные окружения:

| Переменная | Значение |
| --- | --- |
| `WORKSPACE_PATH` | Путь к workspace |
| `EXECUTION_PROMPT_PATH` | Путь к `execution-prompt.md` |
| `EXECUTION_RESULT_PATH` | Путь к папке `implementation/` |
| `EXECUTION_LOG_PATH` | Путь к `execution.log` |

Статусы run:

- `READY_FOR_AGENT` — prompt готов, агент ещё не запускался;
- `RUNNING` — команда агента запущена;
- `SUCCEEDED` — команда завершилась с exit code `0`;
- `FAILED` — команда завершилась с ошибкой, timeout или не смогла стартовать.

По умолчанию `EXECUTION_AGENT_COMMAND` пустой, поэтому автоматический запуск выключен. Это сделано специально: сначала нужно явно выбрать безопасную команду агента.

## Формат фида для Kwork collector

Kwork collector пока ожидает нормализованный JSON-фид. Это позволяет отдельно развивать реальный парсер площадки, не меняя downstream-часть: базу, AI-анализ, scoring и Telegram.

```json
{
  "items": [
    {
      "externalId": "kwork-123",
      "title": "Telegram bot with payments",
      "description": "Need a bot with payments and PostgreSQL.",
      "price": 18000,
      "publishedAt": "2026-08-14T10:00:00Z"
    }
  ]
}
```

Также поддерживается JSON-массив с такой же структурой элементов.

## Формула scoring

```text
score =
    30% * skill match
  + 25% * automation
  + 20% * price/time
  + 15% * simplicity
  + 10% * win probability
```

`price/time` нормализуется относительно `TARGET_HOURLY_RATE`.

`win probability` — консервативная производная оценка на основе соответствия навыкам, автоматизируемости, простоты и риска.

## Следующие продуктовые шаги

1. Собрать 200-500 реальных заказов с Kwork и Freelance.ru.
2. Проанализировать повторяющиеся типы проектов.
3. Выбрать три основные ниши.
4. Сделать три небольших portfolio projects под выбранные ниши.
5. Заменить normalized feed collector на реальные адаптеры площадок.
6. Добавить генерацию откликов только после проверки качества scoring.
7. Сохранять результаты по каждому заказу: откликнулись или нет, получили заказ или нет, сколько часов заняло выполнение, сколько заработали.
