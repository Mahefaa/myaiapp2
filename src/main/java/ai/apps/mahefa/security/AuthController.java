package ai.apps.mahefa.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class AuthController {

  private final JwtService jwtService;

  @PostMapping("/auth/token")
  public TokenResponse getToken(@RequestBody TokenRequest request) {
    // For this showcase, we issue a token to anyone who asks.
    // In a real system, you would verify credentials (e.g., LDAP, DB, Cognito) here.
    String token = jwtService.generateToken(request.getUsername());
    return new TokenResponse(token);
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TokenRequest {
    private String username;
  }

  @Data
  @AllArgsConstructor
  public static class TokenResponse {
    private String token;
  }
}
