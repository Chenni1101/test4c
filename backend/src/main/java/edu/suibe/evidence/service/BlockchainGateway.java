package edu.suibe.evidence.service;

import edu.suibe.evidence.dto.EvidenceCreateRequest;

public interface BlockchainGateway {
  ChainReceipt saveEvidence(EvidenceCreateRequest request);

  ChainReceipt issueAsset(ChainAssetIssueRequest request);

  ChainReceipt recordAuthorization(ChainAuthorizationRequest request);
}
