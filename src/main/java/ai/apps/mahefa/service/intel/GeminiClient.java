package ai.apps.mahefa.service.intel;

import ai.apps.mahefa.endpoint.rest.model.ThreatIntel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@Slf4j
public class GeminiClient {

  private final WebClient webClient;
  private final String apiKey;
  private final ObjectMapper objectMapper;

  public GeminiClient(
      WebClient.Builder webClientBuilder,
      @Value("${gemini.api.key:}") String apiKey,
      ObjectMapper objectMapper) {
    this.webClient = webClientBuilder.baseUrl("https://generativelanguage.googleapis.com").build();
    this.apiKey = apiKey;
    this.objectMapper = objectMapper;
  }

  public ThreatIntel extractThreatIntel(String anonymizedLogs) {
    if (apiKey == null || apiKey.isEmpty()) {
      log.warn("Gemini API key is missing. Returning empty intel.");
      return new ThreatIntel().ips(List.of()).domains(List.of()).summary("API Key missing.");
    }

    String systemPrompt =
        """
        You are a cybersecurity expert. Analyze the following security logs and extract Indicators of Compromise (IOCs).
        Specifically, identify:
        1. Suspicious IP addresses.
        2. Suspicious domains.
        Return ONLY a JSON object with the following structure:
        {
          "ips": ["list of strings"],
          "domains": ["list of strings"],
          "summary": "a short summary of the threat"
        }
        Do not include any other text, markdown formatting, or explanations.
        """;

    Map<String, Object> requestBody =
        Map.of(
            "contents",
            List.of(
                Map.of(
                    "parts",
                    List.of(
                        Map.of("text", systemPrompt + "\n\nLogs to analyze:\n" + anonymizedLogs)))));

    try {
      String responseJson =
          webClient
              .post()
              .uri(uriBuilder -> uriBuilder.path("/v1beta/models/gemini-1.5-flash:generateContent")
                  .queryParam("key", apiKey)
                  .build())
              .contentType(MediaType.APPLICATION_JSON)
              .bodyValue(requestBody)
              .retrieve()
              .bodyToMono(String.class)
              .block();

      return parseGeminiResponse(responseJson);
    } catch (Exception e) {
      log.error("Error calling Gemini API", e);
      return new ThreatIntel().ips(List.of()).domains(List.of()).summary("Error analyzing logs: " + e.getMessage());
    }
  }

  private ThreatIntel parseGeminiResponse(String responseJson) {
    try {
      JsonNode root = objectMapper.readTree(responseJson);
      String textResponse =
          root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

      // Gemini sometimes wraps JSON in markdown blocks
      String cleanedJson = textResponse.replaceAll("```json", "").replaceAll("```", "").trim();

      JsonNode intelNode = objectMapper.readTree(cleanedJson);
      
      List<String> ips = new ArrayList<>();
      intelNode.path("ips").forEach(n -> ips.add(n.asText()));
      
      List<String> domains = new ArrayList<>();
      intelNode.path("domains").forEach(n -> domains.add(n.asText()));
      
      String summary = intelNode.path("summary").asText();

      return new ThreatIntel().ips(ips).domains(domains).summary(summary);
    } catch (Exception e) {
      log.error("Error parsing Gemini response: " + responseJson, e);
      return new ThreatIntel().ips(List.of()).domains(List.of()).summary("Failed to parse AI response.");
    }
  }
}
