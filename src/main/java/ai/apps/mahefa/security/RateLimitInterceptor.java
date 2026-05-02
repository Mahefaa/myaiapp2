package ai.apps.mahefa.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

  private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

  private Bucket createNewBucket() {
    // 5 requests per minute
    Bandwidth limit = Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(1)));
    return Bucket.builder().addLimit(limit).build();
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    
    // Only rate limit the intel endpoint for this showcase
    if (!request.getRequestURI().startsWith("/intel")) {
      return true;
    }

    // Use authenticated username if available, otherwise fallback to IP
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String key = (auth != null) ? auth.getName() : request.getRemoteAddr();

    Bucket bucket = cache.computeIfAbsent(key, k -> createNewBucket());

    if (bucket.tryConsume(1)) {
      return true;
    } else {
      response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
      response.getWriter().write("Too many requests - Rate limit exceeded.");
      return false;
    }
  }
}
