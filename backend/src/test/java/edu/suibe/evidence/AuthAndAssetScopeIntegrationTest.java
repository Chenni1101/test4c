package edu.suibe.evidence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.suibe.evidence.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class AuthAndAssetScopeIntegrationTest {
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private UserRepository userRepository;

  @Test
  void registrationHashesPasswordAndCreatorsCannotReadEachOthersAssets() throws Exception {
    register("creator-scope-a", "creator-a@example.com");
    register("creator-scope-b", "creator-b@example.com");
    String tokenA = login("creator-scope-a");
    String tokenB = login("creator-scope-b");

    assertThat(userRepository.findByUsername("creator-scope-a").orElseThrow().getPasswordHash())
        .isNotEqualTo("SecurePass1234");

    String evidenceRequest =
        """
        {"assetName":"创作者 A 私有资产","assetType":"image","creator":"创作者 A","fileHash":"sha256:%s","timestampIso":"2026-07-26T10:00:00Z"}
        """.formatted("c".repeat(64));
    mockMvc
        .perform(
            post("/api/evidence")
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(evidenceRequest))
        .andExpect(status().isOk());

    mockMvc
        .perform(get("/api/assets").header("Authorization", "Bearer " + tokenB))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isEmpty());
  }

  @Test
  void museumAdminCanManageOnlyAssetsOfItsOwnOrganization() throws Exception {
    long museumAdminId = registerAndReturnId("museum-admin-test", "museum-admin@example.com", "博物馆 A");
    String superAdminToken = login("demo-super-admin", "demo-change-me");
    mockMvc
        .perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(
                    "/api/auth/users/{userId}/role", museumAdminId)
                .header("Authorization", "Bearer " + superAdminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"role\":\"MUSEUM_ADMIN\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.roles[0]").value("MUSEUM_ADMIN"));

    String museumToken = login("museum-admin-test", "SecurePass1234");
    String evidenceRequest =
        """
        {"assetName":"机构资产","assetType":"image","creator":"馆员","organization":"伪造机构","fileHash":"sha256:%s","timestampIso":"2026-07-26T11:00:00Z"}
        """.formatted("d".repeat(64));
    mockMvc
        .perform(
            post("/api/evidence")
                .header("Authorization", "Bearer " + museumToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(evidenceRequest))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.organization").value("博物馆 A"));
  }

  private void register(String username, String email) throws Exception {
    registerAndReturnId(username, email, null);
  }

  private long registerAndReturnId(String username, String email, String organization) throws Exception {
    String payload =
        """
        {"username":"%s","password":"SecurePass1234","displayName":"%s","email":"%s","organization":%s,"role":"CREATOR"}
        """.formatted(username, username, email, organization == null ? "null" : "\"" + organization + "\"");
    MvcResult result =
        mockMvc
        .perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(payload))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.email").value(email))
        .andReturn();
    return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
  }

  private String login(String username) throws Exception {
    return login(username, "SecurePass1234");
  }

  private String login(String username, String password) throws Exception {
    String payload = "{\"username\":\"%s\",\"password\":\"%s\"}".formatted(username, password);
    MvcResult result =
        mockMvc
            .perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(payload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andReturn();
    JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
    return response.get("accessToken").asText();
  }
}
