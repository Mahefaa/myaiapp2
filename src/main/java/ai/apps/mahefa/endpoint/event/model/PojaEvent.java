package ai.apps.mahefa.endpoint.event.model;

import static ai.apps.mahefa.endpoint.event.EventStack.EVENT_STACK_1;
import static java.lang.Math.random;

import ai.apps.mahefa.PojaGenerated;
import ai.apps.mahefa.endpoint.event.EventStack;
import java.io.Serializable;
import java.time.Duration;
import lombok.Getter;
import lombok.Setter;

@PojaGenerated
public abstract class PojaEvent implements Serializable {

  @Getter @Setter protected int attemptNb;

  public abstract Duration maxConsumerDuration();

  public Duration eventHandlerInitMaxDuration() {
    return Duration.ofSeconds(90); // note(init-visibility)
  }

  private Duration randomConsumerBackoffBetweenRetries() {
    return Duration.ofSeconds((int) (random() * maxConsumerBackoffBetweenRetries().toSeconds()));
  }

  public abstract Duration maxConsumerBackoffBetweenRetries();

  public final Duration randomVisibilityTimeout() {
    return Duration.ofSeconds(
        eventHandlerInitMaxDuration().toSeconds()
            + maxConsumerDuration().toSeconds()
            + randomConsumerBackoffBetweenRetries().toSeconds());
  }

  public EventStack getEventStack() {
    return EVENT_STACK_1;
  }

  public String getEventSource() {
    if (getEventStack().equals(EVENT_STACK_1)) return "ai.apps.mahefa.event1";
    return "ai.apps.mahefa.event2";
  }
}
