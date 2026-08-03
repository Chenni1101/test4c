package edu.suibe.evidence.controller;

import edu.suibe.evidence.dto.AssetVersionCreateRequest;
import edu.suibe.evidence.dto.AssetVersionResponse;
import edu.suibe.evidence.dto.AuthorizationResponse;
import edu.suibe.evidence.dto.FileHashVerificationResponse;
import edu.suibe.evidence.dto.ProvenanceReportResponse;
import edu.suibe.evidence.service.AssetLifecycleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@Validated
@RequestMapping("/api")
public class ProvenanceController {
  private final AssetLifecycleService lifecycle;
  private final ObjectMapper objectMapper;
  public ProvenanceController(AssetLifecycleService lifecycle, ObjectMapper objectMapper) { this.lifecycle = lifecycle; this.objectMapper = objectMapper; }

  @GetMapping("/authorizations")
  @PreAuthorize("hasAnyRole('CREATOR', 'MUSEUM_ADMIN', 'SUPER_ADMIN')")
  public List<AuthorizationResponse> listAuthorizations(
      @RequestParam("assetCode") @NotBlank @Pattern(regexp = "^AST-[0-9]{8}-[A-Z0-9]{6}$") String assetCode) {
    return lifecycle.list(assetCode);
  }

  @GetMapping("/assets/{assetCode}/versions")
  @PreAuthorize("hasAnyRole('CREATOR', 'MUSEUM_ADMIN', 'SUPER_ADMIN')")
  public List<AssetVersionResponse> listVersions(
      @PathVariable("assetCode") @Pattern(regexp = "^AST-[0-9]{8}-[A-Z0-9]{6}$") String assetCode) {
    return lifecycle.listVersions(assetCode);
  }

  @PostMapping("/assets/{assetCode}/versions")
  @PreAuthorize("hasAnyRole('CREATOR', 'MUSEUM_ADMIN', 'SUPER_ADMIN')")
  public AssetVersionResponse createVersion(
      @PathVariable("assetCode") @Pattern(regexp = "^AST-[0-9]{8}-[A-Z0-9]{6}$") String assetCode,
      @Valid @RequestBody AssetVersionCreateRequest request) {
    return lifecycle.createVersion(assetCode, request);
  }

  @GetMapping("/assets/{assetCode}/timeline")
  @PreAuthorize("hasAnyRole('CREATOR', 'MUSEUM_ADMIN', 'SUPER_ADMIN')")
  public ProvenanceReportResponse timeline(
      @PathVariable("assetCode") @Pattern(regexp = "^AST-[0-9]{8}-[A-Z0-9]{6}$") String assetCode) {
    return lifecycle.reportByAssetCode(assetCode);
  }

  @GetMapping("/assets/{assetCode}/provenance-report")
  @PreAuthorize("hasAnyRole('CREATOR', 'MUSEUM_ADMIN', 'SUPER_ADMIN')")
  public ResponseEntity<byte[]> export(
      @PathVariable("assetCode") @Pattern(regexp = "^AST-[0-9]{8}-[A-Z0-9]{6}$") String assetCode) throws Exception {
    byte[] body = objectMapper.writeValueAsBytes(lifecycle.reportByAssetCode(assetCode));
    return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(assetCode + "-provenance-report.json", StandardCharsets.UTF_8).build().toString())
        .body(body);
  }

  @GetMapping("/provenance/trace")
  @PreAuthorize("hasAnyRole('CREATOR', 'MUSEUM_ADMIN', 'SUPER_ADMIN')")
  public ProvenanceReportResponse trace(
      @RequestParam(value = "assetCode", required = false) String assetCode,
      @RequestParam(value = "hash", required = false) @Pattern(regexp = "^(sha256:)?[a-fA-F0-9]{64}$", message = "hash 必须是 SHA-256 摘要") String hash,
      @RequestParam(value = "cid", required = false) @Pattern(regexp = "^[A-Za-z0-9._-]{8,255}$") String cid,
      @RequestParam(value = "transactionId", required = false) @Positive Long transactionId) {
    return lifecycle.trace(assetCode, hash, cid, transactionId);
  }

  @PostMapping(value = "/provenance/verify-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasAnyRole('CREATOR', 'MUSEUM_ADMIN', 'SUPER_ADMIN')")
  public FileHashVerificationResponse verifyFile(
      @RequestPart("file") org.springframework.web.multipart.MultipartFile file,
      @RequestParam("expectedHash") @NotBlank @Pattern(regexp = "^(sha256:)?[a-fA-F0-9]{64}$") String expectedHash) {
    return lifecycle.verifyUploadedFile(file, expectedHash);
  }
}
