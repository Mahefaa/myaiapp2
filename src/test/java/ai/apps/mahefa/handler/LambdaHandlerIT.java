package ai.apps.mahefa.handler;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import ai.apps.mahefa.PojaGenerated;
import ai.apps.mahefa.conf.FacadeIT;
import com.amazonaws.services.lambda.runtime.Context;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.jupiter.api.Test;

@PojaGenerated
class LambdaHandlerIT extends FacadeIT {

  @Test
  void lambda_handler_can_be_instantiated_and_called() throws IOException {
    // LambdaHandler manages its own context.
    // By extending FacadeIT, we ensure system properties are set and infrastructure (Postgres) is ready.
    LambdaHandler lambdaHandler = new LambdaHandler();
    assertNotNull(lambdaHandler);

    // Minimal HTTP API V2 event for /ping
    String event = "{\"version\":\"2.0\",\"rawPath\":\"/ping\",\"requestContext\":{\"http\":{\"method\":\"GET\"}}}";
    ByteArrayInputStream inputStream = new ByteArrayInputStream(event.getBytes());
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Context context = mock(Context.class);

    lambdaHandler.handleRequest(inputStream, outputStream, context);

    String response = outputStream.toString();
    assertNotNull(response);
    // The response should contain a 200 status code for /ping
    org.junit.jupiter.api.Assertions.assertTrue(response.contains("\"statusCode\":200"));
  }
}
