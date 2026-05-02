package ai.apps.mahefa.service.intel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class AnonymizerService {

  // Simple regex for email redaction
  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+");

  /**
   * Scrubs PII from the input string to protect user privacy before sending data to an LLM.
   *
   * @param input Raw log or text containing potential PII.
   * @return Anonymized text.
   */
  public String anonymize(String input) {
    if (input == null) {
      return null;
    }

    String anonymized = input;
    anonymized = redactEmails(anonymized);
    // Future expansion: add redaction for IP addresses (not IOC ones), credit cards, etc.

    return anonymized;
  }

  private String redactEmails(String input) {
    Matcher matcher = EMAIL_PATTERN.matcher(input);
    return matcher.replaceAll("[REDACTED_EMAIL]");
  }
}
