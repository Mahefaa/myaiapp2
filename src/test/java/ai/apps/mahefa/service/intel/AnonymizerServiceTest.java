package ai.apps.mahefa.service.intel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AnonymizerServiceTest {

  private final AnonymizerService anonymizerService = new AnonymizerService();

  @Test
  void anonymize_redacts_emails() {
    String input = "User at john.doe@example.com logged in from 192.168.1.1";
    String result = anonymizerService.anonymize(input);
    
    assertTrue(result.contains("[REDACTED_EMAIL]"));
    assertTrue(!result.contains("john.doe@example.com"));
    assertTrue(result.contains("192.168.1.1")); // IPs (potential IOCs) should be preserved
  }

  @Test
  void anonymize_handles_multiple_emails() {
    String input = "From: alice@test.com To: bob@work.org";
    String result = anonymizerService.anonymize(input);
    
    assertEquals("From: [REDACTED_EMAIL] To: [REDACTED_EMAIL]", result);
  }

  @Test
  void anonymize_handles_null() {
    assertEquals(null, anonymizerService.anonymize(null));
  }
}
