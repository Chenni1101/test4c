package edu.suibe.evidence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.suibe.evidence.repository.AuditLogRepository;
import edu.suibe.evidence.security.JwtService;
import edu.suibe.evidence.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {
  @Autowired private MockMvc mockMvc;
  @Autowired private AuditLogRepository auditLogRepository;
  @Autowired private JwtService jwtService;
  @Autowired private UserDetailsServiceImpl userDetailsService;

  @Test
  void protectedRoutesRequireJwtAndAuthenticatedUserCanInspectSelf() throws Exception {
    mockMvc.perform(get("/api/health")).andExpect(status().isOk());
    mockMvc
        .perform(get("/api/assets"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));

    mockMvc
        .perform(get("/api/auth/me").header("Authorization", bearerFor("demo-super-admin")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("demo-super-admin"))
        .andExpect(jsonPath("$.roles[0]").value("SUPER_ADMIN"));
  }

  @Test
  void evidenceRequestRequiresValidPayloadAndWritesAuthenticatedActorToAudit() throws Exception {
    String invalidRequest =
        """
        {"assetName":"测试资产","assetType":"image","creator":"测试团队","fileHash":"sha256:invalid","timestampIso":"2026-07-26T09:00:00Z"}
        """;

    mockMvc
        .perform(
            post("/api/evidence")
                .header("Authorization", bearerFor("demo-super-admin"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

    String validRequest =
        """
        {"assetName":"测试资产","assetType":"image","creator":"测试团队","fileHash":"sha256:%s","timestampIso":"2026-07-26T09:00:00Z"}
        """.formatted("a".repeat(64));
    mockMvc
        .perform(
            post("/api/evidence")
                .header("Authorization", bearerFor("demo-super-admin"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequest))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.mode").value("DEMO"));

    assertThat(auditLogRepository.findAll())
        .anySatisfy(
            log -> {
              assertThat(log.getAction()).isEqualTo("CREATE_EVIDENCE");
              assertThat(log.getOperatorName()).isEqualTo("demo-super-admin");
            });
  }

  private String bearerFor(String username) {
    return "Bearer " + jwtService.generate(userDetailsService.loadUserByUsername(username));
  }
}
