package edu.suibe.evidence.service;

import edu.suibe.evidence.dto.EvidenceCreateRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Production integration boundary. It deliberately rejects requests until a verified XuperChain
 * SDK/HTTP adapter and credential source are configured; it never fabricates a chain receipt.
 */
@Component
@ConditionalOnProperty(name = "evidence.chain.mode", havingValue = "REAL")
public class RealXuperChainGateway implements BlockchainGateway {
  @Override
  public ChainReceipt saveEvidence(EvidenceCreateRequest request) {
    throw new IllegalStateException("REAL 链网关尚未配置可验证的 XuperChain 客户端");
  }

  @Override
  public ChainReceipt issueAsset(ChainAssetIssueRequest request) {
    throw new IllegalStateException("REAL 链网关尚未配置可验证的 XuperChain 客户端");
  }

  @Override
  public ChainReceipt recordAuthorization(ChainAuthorizationRequest request) {
    throw new IllegalStateException("REAL 链网关尚未配置可验证的 XuperChain 客户端");
  }
}
