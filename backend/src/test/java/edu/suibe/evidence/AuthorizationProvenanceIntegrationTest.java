package edu.suibe.evidence;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.suibe.evidence.entity.AuthorizationEntity;
import edu.suibe.evidence.repository.AuthorizationRepository;
import edu.suibe.evidence.service.AssetLifecycleService;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class AuthorizationProvenanceIntegrationTest {
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private AuthorizationRepository authorizationRepository;
  @Autowired private AssetLifecycleService lifecycleService;

  @Test
  void authorizationIsRecordedAsDemoAndDuplicateIsRejected() throws Exception {
    String token = registerAndLogin("auth-owner-" + unique(), "auth-owner-" + unique() + "@example.com");
    String assetCode = issue(token, "1");
    String request = authorizationRequest(assetCode, Instant.now().minusSeconds(60), Instant.now().plusSeconds(86_400));

    MvcResult result = mockMvc.perform(post("/api/authorizations").header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON).content(request))
        .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("ACTIVE"))
        .andExpect(jsonPath("$.mode").value("DEMO")).andExpect(jsonPath("$.chainTxId").value(org.hamcrest.Matchers.startsWith("demo_auth_tx_")))
        .andReturn();
    String authorizationId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();

    mockMvc.perform(post("/api/authorizations").header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON).content(request))
        .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("STATE_CONFLICT"));

    mockMvc.perform(post("/api/authorizations/{id}/revoke", authorizationId).header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"合作终止\"}"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("REVOKED"));

    mockMvc.perform(get("/api/assets/{assetCode}/timeline", assetCode).header("Authorization", "Bearer " + token))
        .andExpect(status().isOk()).andExpect(jsonPath("$.timeline").isArray());
  }

  @Test
  void expiredAuthorizationIsRetainedAndMarkedExpired() throws Exception {
    String token = registerAndLogin("expire-owner-" + unique(), "expire-owner-" + unique() + "@example.com");
    String assetCode = issue(token, "2");
    MvcResult result = mockMvc.perform(post("/api/authorizations").header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON).content(authorizationRequest(assetCode, Instant.now().minusSeconds(60), Instant.now().plusSeconds(3600))))
        .andExpect(status().isOk()).andReturn();
    String authorizationId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    AuthorizationEntity authorization = authorizationRepository.findById(authorizationId).orElseThrow();
    authorization.setEndsAt(Instant.now().minusSeconds(1));
    authorizationRepository.save(authorization);
    lifecycleService.expireDueAuthorizations();

    mockMvc.perform(get("/api/authorizations").param("assetCode", assetCode).header("Authorization", "Bearer " + token))
        .andExpect(status().isOk()).andExpect(jsonPath("$[0].status").value("EXPIRED"));
  }

  @Test
  void creatorCannotAuthorizeAnotherCreatorsAsset() throws Exception {
    String ownerToken = registerAndLogin("scope-owner-" + unique(), "scope-owner-" + unique() + "@example.com");
    String outsiderToken = registerAndLogin("scope-outsider-" + unique(), "scope-outsider-" + unique() + "@example.com");
    String assetCode = issue(ownerToken, "3");
    mockMvc.perform(post("/api/authorizations").header("Authorization", "Bearer " + outsiderToken)
            .contentType(MediaType.APPLICATION_JSON).content(authorizationRequest(assetCode, Instant.now().minusSeconds(60), Instant.now().plusSeconds(3600))))
        .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));
  }

  private String issue(String token, String digit) throws Exception {
    String hash = digit.repeat(64);
    String payload = """
        {"assetName":"授权测试资产","assetType":"image","creator":"测试创作者","organization":"测试机构","contentSha256":"%s","originalFilename":"asset.png","mimeType":"image/png","fileSizeBytes":128}
        """.formatted(hash);
    MvcResult result = mockMvc.perform(post("/api/assets/issue").header("Authorization", "Bearer " + token)
            .header("Idempotency-Key", "auth-issue-" + unique()).contentType(MediaType.APPLICATION_JSON).content(payload))
        .andExpect(status().isOk()).andReturn();
    return objectMapper.readTree(result.getResponse().getContentAsString()).get("assetCode").asText();
  }

  private String authorizationRequest(String assetCode, Instant startsAt, Instant endsAt) {
    return """
        {"assetCode":"%s","licenseeName":"受授权机构","licenseeOrganization":"测试合作方","usageType":"COMMERCIAL","commercial":true,"revenueShareRatio":12.50,"startsAt":"%s","endsAt":"%s"}
        """.formatted(assetCode, startsAt, endsAt);
  }

  private String registerAndLogin(String username, String email) throws Exception {
    String register = """
        {"username":"%s","password":"SecurePass1234","displayName":"测试用户","email":"%s","role":"CREATOR"}
        """.formatted(username, email);
    mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(register)).andExpect(status().isCreated());
    MvcResult login = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\"%s\",\"password\":\"SecurePass1234\"}".formatted(username)))
        .andExpect(status().isOk()).andReturn();
    return objectMapper.readTree(login.getResponse().getContentAsString()).get("accessToken").asText();
  }

  private String unique() { return UUID.randomUUID().toString().replace("-", "").substring(0, 10); }
}
