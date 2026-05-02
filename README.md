# poja-scheduled-mailing-template — cron-triggered tasks for Spring Boot

A [Poja](https://poja.io) starter template with **AWS EventBridge scheduled tasks** pre-configured. Define a cron expression in the dashboard — Poja fires your async worker automatically, no cron daemon or scheduler to run.

→ **[Full guide on docs.poja.io](https://docs.poja.io/docs/hello-world-but-with-scheduled-tasks)**

Or hit the `Deploy to Poja` button to **deploy this template on your account** :

[![Deploy on Poja](https://img.shields.io/badge/Deploy%20On%20Poja-007BFF?style=for-the-badge)](https://console.poja.io/applications/create/clone/?templateId=ede01491-2862-40f9-b356-b2c73b17a929)

---

### What you get

Write one event class. Configure the schedule in the Poja console — no code change needed to update timing.

```java
// The event — in endpoint.event.model
public class DailyReportTriggered extends PojaEvent {
  @Override public Duration maxConsumerDuration() { return Duration.ofSeconds(20); }
  @Override public Duration maxConsumerBackoffBetweenRetries() { return Duration.ofMinutes(1); }
}

// The consumer — in service.event
@Service @AllArgsConstructor
public class DailyReportTriggeredService implements Consumer<DailyReportTriggered> {
  private final Mailer mailer;

  @Override
  public void accept(DailyReportTriggered event) {
    // your logic here — runs automatically on schedule
  }
}
```

Then in the Poja console → Scheduled Tasks → set **class name** + **cron expression**:

```
cron(0 6 * * ? *)   →  every day at 6:00 AM UTC
```

> Part of the [Poja platform](https://poja.io) — deploy Spring Boot without DevOps.
