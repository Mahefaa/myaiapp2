package ai.apps.mahefa.endpoint.event.consumer.model;

import ai.apps.mahefa.PojaGenerated;
import ai.apps.mahefa.endpoint.event.model.PojaEvent;

@PojaGenerated
public record TypedEvent(String typeName, PojaEvent payload) {}
