package edu.suibe.evidence.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.suibe.evidence.controller.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
  private final ObjectMapper objectMapper;

  public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authenticationException)
      throws IOException {
    write(response, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHENTICATED", "需要有效身份认证", request);
  }

  private void write(
      HttpServletResponse response,
      int status,
      String code,
      String message,
      HttpServletRequest request)
      throws IOException {
    response.setStatus(status);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    objectMapper.writeValue(
        response.getOutputStream(),
        new ApiErrorResponse(
            Instant.now(), status, code, message, request.getHeader("X-Request-Id"), Map.of()));
  }
}
