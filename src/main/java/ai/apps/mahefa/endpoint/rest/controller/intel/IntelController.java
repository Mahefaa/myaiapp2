package ai.apps.mahefa.endpoint.rest.controller.intel;

import ai.apps.mahefa.endpoint.rest.model.ExtractIntelRequest;
import ai.apps.mahefa.endpoint.rest.model.ThreatIntel;
import ai.apps.mahefa.service.intel.AnonymizerService;
import ai.apps.mahefa.service.intel.GeminiClient;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class IntelController {

  private final AnonymizerService anonymizerService;
  private final GeminiClient geminiClient;

  @PostMapping("/intel/extract")
  public ThreatIntel extractIntel(@RequestBody ExtractIntelRequest request) {
    // 1. Anonymize data before it leaves the secure environment (Cybersecurity of AI)
    String anonymizedLogs = anonymizerService.anonymize(request.getRawLogs());

    // 2. Call Gemini for extraction
    return geminiClient.extractThreatIntel(anonymizedLogs);
  }
}
