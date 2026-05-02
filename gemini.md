# gemini.md — Platform Brief for AI-Assisted Development

> You are working on a Spring Boot Java backend running on AWS Lambda.
> This file tells you exactly what infrastructure you have, what the constraints are, and how the pieces connect.
> **The product idea is yours to define.** It must be AI-enabled. Everything else is up to you.

---

## What You Are Working With

This is a **production-grade, serverless Spring Boot 3.2.2 / Java 21 backend** scaffolded by [Poja](https://poja.io). It deploys to AWS Lambda with no DevOps required. CI/CD is already configured in `.github/workflows/`.

You have **three Lambda functions** at your disposal, a **PostgreSQL database**, **file storage**, **email delivery**, and **full internet access**. The following sections describe each in detail.

---

## The Three Lambda Functions

### Frontal Function — HTTP Gateway

This is where all client HTTP requests land. It runs Spring Boot inside a `SpringBootLambdaContainerHandler` (see `handler/LambdaHandler.java`).

- **Max runtime:** 15 minutes, but aim for fast. Clients are waiting.
- **Sweet spot:** Validate input → persist minimal state → delegate heavy work to a worker → return immediately.
- REST endpoints are defined in `doc/api.yml` (OpenAPI 3.0). A Java client and a TypeScript/Axios client are auto-generated from this spec at build time via the Gradle `generateJavaClient` / `generateTsClient` tasks.
- Add new endpoints by editing `api.yml` and creating a Spring `@RestController` under `endpoint/rest/controller/`.

### Worker Function 1 — Async Processing (EVENT_STACK_1)

Triggered by events sent to its SQS queue (`AWS_EVENT_STACK_1_SQS_QUEUE_URL`). Events are routed via AWS EventBridge.

- **No time pressure.** Use this for anything that takes a while: calling external APIs, processing files, running chains of LLM calls, etc.
- To add a new job type: create a class extending `PojaEvent` (set `getEventStack()` to `EVENT_STACK_1`), and a corresponding `@Service` implementing `Consumer<YourEvent>` under `service/event/`.
- Built-in retry with configurable backoff via `maxConsumerDuration()` and `maxConsumerBackoffBetweenRetries()` on the event class.

### Worker Function 2 — Async Processing (EVENT_STACK_2)

Identical mechanism to Worker 1, but backed by a separate SQS queue (`AWS_EVENT_STACK_2_SQS_QUEUE_URL`).

- Use it for a second stage of async work: post-processing, notification, delivery, or anything that should run after Worker 1 finishes.
- Route an event to it by overriding `getEventStack()` to return `EVENT_STACK_2`.

### How the Three Functions Talk to Each Other

```
Client
  │
  ▼
[Frontal]  →  (fire PojaEvent)  →  EventBridge  →  SQS  →  [Worker 1]
                                                              │
                                                              └──  (fire another PojaEvent)  →  SQS  →  [Worker 2]
```

The frontal fires events using `EventProducer<T>`. Workers consume them via their `Consumer<T>` service. Workers can also fire events to chain further processing.

---

## Infrastructure You Can Use

### PostgreSQL Database

- Managed via **Flyway** migrations in `src/main/resources/db/migration/`.
- Add a new migration file (`V<number>__description.sql`) to create tables, add columns, etc.
- Spring Data JPA is on the classpath. Add entities and repositories under `repository/`.
- The existing `Dummy` and `DummyUuid` tables are scaffolding — replace them with your domain model.

### S3 File Storage

- `file/bucket/BucketComponent.java` wraps the AWS SDK v2 S3 client.
- Upload and download arbitrary files (user uploads, generated outputs, intermediate artifacts, etc.).
- Apache Tika (`org.apache.tika:tika-core`) is already on the classpath for MIME type detection.

### Email (SES)

- `mail/Mailer.java` sends emails via AWS SES.
- `mail/EmailAddressVerifier.java` validates addresses before sending.
- Use for transactional emails: notifications, results delivery, digests, alerts.

### Scheduled Tasks (EventBridge Cron)

- Configure a cron expression in the Poja console to trigger any `PojaEvent` automatically on a schedule.
- No code change needed to update the timing — just set the class name and cron expression in the UI.
- Example use: nightly batch jobs, periodic syncs, scheduled AI report generation.

### Concurrency (Virtual Threads)

- `concurrency/Workers.java` uses Java 21 virtual threads (`newVirtualThreadPerTaskExecutor()`).
- Use `workers.invokeAll(List<Callable<Void>>)` to fan out parallel work inside a single Lambda invocation.
- Useful for calling multiple APIs in parallel, processing batches simultaneously, etc.

### Internet Access

- All three Lambda functions have **full outbound internet access**.
- Call any external API: OpenAI, Anthropic, Google Gemini, Replicate, Stability AI, a scraping service, a translation API, anything.
- Use standard Java HTTP clients or any SDK you add to `build.gradle`.

---

## Key Source Structure

```
src/main/java/ai/apps/mahefa/
├── handler/
│   ├── LambdaHandler.java              ← frontal entrypoint
│   └── MailboxEventHandler.java        ← worker entrypoint (both stacks)
├── endpoint/
│   ├── event/
│   │   ├── EventProducer.java          ← fire events to EventBridge
│   │   ├── EventStack.java             ← STACK_1 / STACK_2 enum → SQS URLs
│   │   └── model/PojaEvent.java        ← base class for all async events
│   └── rest/controller/                ← put your HTTP controllers here
├── service/event/                      ← put your Consumer<T> workers here
├── repository/                         ← Spring Data JPA repositories
├── file/bucket/BucketComponent.java    ← S3 helpers
├── mail/Mailer.java                    ← SES email
└── concurrency/Workers.java            ← parallel virtual-thread executor
```

---

## What to Build: Your Call

The only requirement is that the product **leverages AI in a meaningful way** — not as a gimmick, but as the core value delivered to the user.

Some directions to consider (non-exhaustive, feel free to ignore all of these):

- A chatbot or conversational assistant backed by an LLM, with conversation history persisted in Postgres
- A document or content processing pipeline (summarization, extraction, transformation, translation)
- An AI agent that takes a high-level goal, breaks it into steps, and executes them asynchronously via workers
- A code review, code generation, or "explain this codebase" tool for developers
- An image or media generation service (generate → store in S3 → return a download URL)
- A scheduled AI digest (pull data on a cron, summarize with an LLM, email results to subscribers)
- A semantic search engine over user-provided content
- A "bring AI to your existing data" connector that ingests, embeds, and queries structured or unstructured data
- A voice or audio transcription pipeline
- Something entirely different that uses the infrastructure in a creative way

When you have decided what to build, follow this order:

1. **Define the domain model** — what tables do you need in Postgres? Write Flyway migrations.
2. **Define the API** — what endpoints does the frontal expose? Add them to `doc/api.yml`.
3. **Define the events** — what async jobs exist? Create `PojaEvent` subclasses.
4. **Implement the workers** — one `Consumer<T>` service per event type.
5. **Wire the AI calls** — add the relevant SDK to `build.gradle` and call it from the worker(s).
6. **Use the frontal for fast I/O only** — accept, validate, enqueue, return. Keep it snappy.

---

## Constraints and Good Practices

- **Frontal should be fast.** If something takes more than a couple of seconds, it belongs in a worker.
- **Workers are where the AI work happens.** They have time. Use it.
- **Everything is Java.** No other language. Stay within the Spring Boot ecosystem.
- **Flyway for all schema changes.** Never modify the DB manually; always add a migration file.
- **OpenAPI-first for endpoints.** Define the contract in `api.yml` before writing the controller.
- **One `Consumer<T>` per event type.** Keep services focused and testable.
- **Retry is free.** Design events to be idempotent — they may be consumed more than once.
- **The project is for sale.** Build something genuinely valuable. Think about who pays, why, and how much.
