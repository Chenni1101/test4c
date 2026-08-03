package edu.suibe.evidence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.suibe.evidence.security.JwtService;
import edu.suibe.evidence.security.UserDetailsServiceImpl;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class AssetIssuanceIntegrationTest {
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private JwtService jwtService;
  @Autowired private UserDetailsServiceImpl userDetailsService;

  @Test
  void issueIsIdempotentAndClearlyLabelsDemoStorageAndChainReceipt() throws Exception {
    String token = bearerForSuperAdmin();
    String request = issueRequest("演示发行资产", "e".repeat(64));
    MvcResult issued =
        mockMvc
            .perform(
                post("/api/assets/issue")
                    .header("Authorization", token)
                    .header("Idempotency-Key", "issue-demo-0001")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.assetCode").value(org.hamcrest.Matchers.startsWith("AST-")))
            .andExpect(jsonPath("$.cid").value(org.hamcrest.Matchers.startsWith("demo-cid-")))
            .andExpect(jsonPath("$.storageMode").value("DEMO"))
            .andExpect(jsonPath("$.chainStatus").value("CONFIRMED"))
            .andExpect(jsonPath("$.notice").value(org.hamcrest.Matchers.containsString("Demo/Mock")))
            .andReturn();

    JsonNode response = objectMapper.readTree(issued.getResponse().getContentAsString());
    String assetCode = response.get("assetCode").asText();
    String cid = response.get("cid").asText();
    long transactionId = response.get("chainTransactionId").asLong();

    mockMvc
        .perform(
            post("/api/assets/issue")
                .header("Authorization", token)
                .header("Idempotency-Key", "issue-demo-0001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.assetCode").value(assetCode));

    mockMvc
        .perform(get("/api/assets/ipfs/{cid}", cid).header("Authorization", token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.mode").value("DEMO"))
        .andExpect(jsonPath("$.notice").value(org.hamcrest.Matchers.containsString("不是真实 IPFS")));

    mockMvc
        .perform(get("/api/assets/chain-transactions/{id}", transactionId).header("Authorization", token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.mode").value("DEMO"));
  }

  @Test
  void batchIssueAcceptsTenAssets() throws Exception {
    String assets =
        IntStream.range(0, 10)
            .mapToObj(index -> issueRequest("批量资产-" + index, String.format("%064x", index + 100)))
            .collect(Collectors.joining(","));
    mockMvc
        .perform(
            post("/api/assets/issue/batch")
                .header("Authorization", bearerForSuperAdmin())
                .header("Idempotency-Key", "issue-batch-0001")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"assets\":[" + assets + "]}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(10))
        .andExpect(jsonPath("$[0].storageMode").value("DEMO"));
  }

  private String issueRequest(String assetName, String hash) {
    return """
        {"assetName":"%s","assetType":"image","creator":"测试团队","organization":"测试机构","contentSha256":"%s","originalFilename":"asset.png","mimeType":"image/png","fileSizeBytes":1024}
        """.formatted(assetName, hash);
  }

  private String bearerForSuperAdmin() {
    return "Bearer " + jwtService.generate(userDetailsService.loadUserByUsername("demo-super-admin"));
  }
}
