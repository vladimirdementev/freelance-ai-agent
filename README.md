# Freelance AI Agent

MVP backend for finding freelance orders, classifying them, scoring the best opportunities, and sending top matches to Telegram.

The first version is intentionally narrow:

1. collect orders from one source boundary;
2. normalize and save them to PostgreSQL;
3. classify each order with OpenAI when configured, otherwise with a deterministic fallback classifier;
4. calculate a score;
5. notify Telegram when a project crosses the configured score threshold.

No automatic proposal sending, CRM, mobile app, or AI coding agent is included in this MVP.

## Architecture

```text
Kwork/feed or parser
  -> /api/projects/ingest
  -> PostgreSQL
  -> AI/heuristic classifier
  -> scoring engine
  -> top projects API
  -> Telegram notification
```

## Tech stack

- Java 21
- Spring Boot 4.1
- PostgreSQL
- Flyway
- Maven
- Docker Compose

## Run locally

```bash
docker compose up --build
```

Optional environment variables:

| Variable | Purpose | Default |
| --- | --- | --- |
| `OPENAI_API_KEY` | Enables OpenAI classification | empty |
| `OPENAI_MODEL` | OpenAI model name | `gpt-4.1-mini` |
| `TELEGRAM_BOT_TOKEN` | Telegram bot token | empty |
| `TELEGRAM_CHAT_ID` | Telegram chat id | empty |
| `TARGET_HOURLY_RATE` | RUB/hour target for price/time scoring | `1500` |
| `MIN_NOTIFICATION_SCORE` | Minimum score for Telegram notification | `80` |
| `COLLECTORS_ENABLED` | Enables scheduled collectors | `false` |
| `KWORK_FEED_URL` | URL for a normalized Kwork JSON feed | empty |

## API

### Ingest one normalized project

```bash
curl -X POST http://localhost:8080/api/projects/ingest \
  -H 'Content-Type: application/json' \
  -d '{
    "platform": "KWORK",
    "externalId": "kwork-123",
    "title": "Telegram bot with payments",
    "description": "Need a Telegram bot with catalog, PostgreSQL, payment API and admin notifications.",
    "price": 18000,
    "publishedAt": "2026-08-14T10:00:00Z"
  }'
```

### Get top projects

```bash
curl 'http://localhost:8080/api/projects/top?limit=10'
```

## Collector feed format

The first collector expects a normalized JSON feed so the parsing boundary can evolve independently from analysis and scoring:

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

An array with the same item shape is also accepted.

## Scoring formula

```text
score =
    30% * skill match
  + 25% * automation
  + 20% * price/time
  + 15% * simplicity
  + 10% * win probability
```

`price/time` is normalized by `TARGET_HOURLY_RATE`. `win probability` is a conservative derived score based on skill match, automation, simplicity, and risk.

## Next product milestones

1. Collect 200-500 real orders from Kwork and Freelance.ru.
2. Analyze repeated project types and choose three niches.
3. Build three small portfolio projects.
4. Replace the normalized feed collector with real platform adapters.
5. Add proposal generation after the scoring quality is validated.
6. Record outcomes for every order to improve future scoring.
