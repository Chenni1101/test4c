package edu.suibe.evidence.controller;

import edu.suibe.evidence.dto.EvidenceCreateRequest;
import edu.suibe.evidence.dto.EvidenceResponse;
import edu.suibe.evidence.service.EvidenceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 版权存证RESTful API控制器
 * 对外提供标准HTTP接口，接收前端请求并返回响应
 * 所有接口参数都经过JSR-380校验，保证输入合法性
 */
@RestController
@RequestMapping("/api")
@Validated
public class EvidenceController {
  private final EvidenceService evidenceService;

  public EvidenceController(EvidenceService evidenceService) {
    this.evidenceService = evidenceService;
  }

  /**
   * 创建版权存证接口
   * 对应前端存证页面的"提交存证"按钮
   * @param request 存证请求参数（自动校验必填字段）
   * @return 存证结果（包含交易ID、区块高度等）
   */
  @PostMapping("/evidence")
  @PreAuthorize("hasAnyRole('CREATOR', 'MUSEUM_ADMIN', 'SUPER_ADMIN')")
  public EvidenceResponse create(@Valid @RequestBody EvidenceCreateRequest request) {
    return evidenceService.createEvidence(request);
  }

  /**
   * 根据哈希查询存证接口
   * 对应前端查询页的"按哈希查询"功能
   * @param hash 文件SHA-256哈希
   * @return 存证详情
   */
  @GetMapping("/evidence/{hash}")
  public EvidenceResponse getByHash(
      @PathVariable
          @Pattern(
              regexp = "^sha256:[a-fA-F0-9]{64}$",
              message = "hash 必须是 sha256: 前缀的 64 位十六进制摘要")
          String hash) {
    return evidenceService.getEvidenceByHash(hash);
  }

  /**
   * 获取所有资产列表接口
   * 对应前端资产管理页的资产列表展示
   * @return 所有存证资产的列表
   */
  @GetMapping("/assets")
  public List<EvidenceResponse> listAssets() {
    return evidenceService.listEvidence();
  }
}
