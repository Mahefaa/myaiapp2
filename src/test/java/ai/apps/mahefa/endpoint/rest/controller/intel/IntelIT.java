package ai.apps.mahefa.endpoint.rest.controller.intel;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.apps.mahefa.conf.FacadeIT;
import ai.apps.mahefa.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class IntelIT extends FacadeIT {

  @Autowired private MockMvc mockMvc;
  @Autowired private JwtService jwtService;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void extract_intel_without_auth_fails() throws Exception {
    mockMvc
        .perform(
            post("/intel/extract")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("rawLogs", "some logs"))))
        .andExpect(status().isForbidden()); // Spring Security default for unauthorized if not configured otherwise
  }

  @Test
  void extract_intel_with_auth_succeeds_layer_wise() throws Exception {
    String token = jwtService.generateToken("user_for_ok_test");
    
    // If Gemini key is missing, it returns a 200 with an empty list as per our code.
    mockMvc
        .perform(
            post("/intel/extract")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("rawLogs", "some logs"))))
        .andExpect(status().isOk());
  }

  @Test
  void rate_limiting_works() throws Exception {
    String token = jwtService.generateToken("user_for_rate_limit_test");
    
    // The limit is 5 per minute
    for (int i = 0; i < 5; i++) {
      mockMvc
          .perform(
              post("/intel/extract")
                  .header("Authorization", "Bearer " + token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(Map.of("rawLogs", "some logs"))))
          .andExpect(status().isOk());
    }
    
    // 6th request should fail
    mockMvc
        .perform(
            post("/intel/extract")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("rawLogs", "some logs"))))
        .andExpect(status().isTooManyRequests());
  }
}
