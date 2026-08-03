package edu.suibe.evidence.controller;

import edu.suibe.evidence.dto.AssetCredentialResponse;
import edu.suibe.evidence.dto.AssetIssueRequest;
import edu.suibe.evidence.dto.BatchAssetIssueRequest;
import edu.suibe.evidence.dto.ChainTransactionResponse;
import edu.suibe.evidence.dto.IpfsFileResponse;
import edu.suibe.evidence.service.AssetIssuanceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/assets")
public class AssetIssuanceController {
  private final AssetIssuanceService issuanceService;

  public AssetIssuanceController(AssetIssuanceService issuanceService) {
    this.issuanceService = issuanceService;
  }

  @PostMapping("/issue")
  @PreAuthorize("hasAnyRole('CREATOR', 'MUSEUM_ADMIN', 'SUPER_ADMIN')")
  public AssetCredentialResponse issue(
      @RequestHeader("Idempotency-Key")
          @NotBlank
          @Pattern(regexp = "^[A-Za-z0-9._-]{8,128}$")
          String idempotencyKey,
      @Valid @RequestBody AssetIssueRequest request) {
    return issuanceService.issue(request, idempotencyKey);
  }

  @PostMapping("/issue/batch")
  @PreAuthorize("hasAnyRole('CREATOR', 'MUSEUM_ADMIN', 'SUPER_ADMIN')")
  public List<AssetCredentialResponse> issueBatch(
      @RequestHeader("Idempotency-Key")
          @NotBlank
          @Pattern(regexp = "^[A-Za-z0-9._-]{8,124}$")
          String idempotencyKey,
      @Valid @RequestBody BatchAssetIssueRequest request) {
    return issuanceService.issueBatch(request.assets(), idempotencyKey);
  }

  @PostMapping("/issuances/{transactionId}/retry")
  @PreAuthorize("hasAnyRole('CREATOR', 'MUSEUM_ADMIN', 'SUPER_ADMIN')")
  public AssetCredentialResponse retry(@PathVariable("transactionId") @Positive Long transactionId) {
    return issuanceService.retry(transactionId);
  }

  @GetMapping("/ipfs/{cid}")
  public IpfsFileResponse findCid(
      @PathVariable("cid") @NotBlank @Pattern(regexp = "^[A-Za-z0-9._-]{8,255}$") String cid) {
    return issuanceService.findCid(cid);
  }

  @GetMapping("/chain-transactions/{transactionId}")
  public ChainTransactionResponse transaction(@PathVariable("transactionId") @Positive Long transactionId) {
    return issuanceService.findTransaction(transactionId);
  }
}
