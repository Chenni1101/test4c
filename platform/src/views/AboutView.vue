<template>
  <div class="about-page">
    <div class="about-hero">
      <div class="hero-content">
        <span class="hero-kicker">平台概览</span>
        <h1>关于项目</h1>
        <p>
          本系统面向文旅数字资产的确权、存证、查询与交易协作场景，
          以区块链可信存证为核心能力，构建可验证、可追溯、可协作的文化资产管理平台
        </p>
        <div class="hero-tags">
          <span>文旅数字资产</span>
          <span>区块链可信存证</span>
          <span>馆校协作</span>
        </div>
      </div>
      <div class="hero-card">
        <h3>项目定位</h3>
        <p>面向馆藏机构与文旅场景，强调可信证据链与可视化运营能力</p>
        <div class="hero-metrics">
          <div>
            <span>可信链路</span>
            <strong>4步闭环</strong>
          </div>
          <div>
            <span>检索效率</span>
            <strong>O(1)</strong>
          </div>
          <div>
            <span>存证成本</span>
            <strong>¥0.01</strong>
          </div>
        </div>
      </div>
    </div>

    <div class="about-grid">
      <div class="about-card">
        <h3>项目使命</h3>
        <p>让文旅资产在数字时代“可被看见、可被信任、可被传播”</p>
      </div>
      <div class="about-card">
        <h3>核心价值</h3>
        <ul>
          <li>确权更快：秒级生成哈希指纹与上链存证</li>
          <li>验证更准：链上证据可追溯，信息可复验</li>
          <li>协作更强：支持跨馆授权与交易可视化</li>
        </ul>
      </div>
      <div class="about-card">
        <h3>服务对象</h3>
        <p>博物馆、文化展陈、文旅运营机构与数字藏品平台</p>
      </div>
    </div>

    <div class="about-section">
      <div class="section-header">
        <h2>技术创新点</h2>
        <p>以可信链路 + 场景化运营构建差异化产品亮点</p>
      </div>
      <div class="highlight-grid">
        <div class="highlight-card">
          <h4>双索引检索结构</h4>
          <p>哈希索引与资产索引协同，兼顾速度与可扩展性</p>
        </div>
        <div class="highlight-card">
          <h4>证据链路可视化</h4>
          <p>从采集、上链到交易呈现，完整展示可信证据链</p>
        </div>
        <div class="highlight-card">
          <h4>文旅协作授权模型</h4>
          <p>面向多馆协作与授权交易，沉淀可复用的场景策略</p>
        </div>
      </div>
    </div>

    <div class="about-section">
      <div class="section-header">
        <h2>技术栈与能力</h2>
        <p>稳定的前端交互、可信的链上存证与可视化分析能力</p>
      </div>
      <div class="stack-tags">
        <span>Vue 3</span>
        <span>Ant Design Vue</span>
        <span>Vite</span>
        <span>ECharts</span>
        <span>区块链存证</span>
        <span>AI 场景分析</span>
      </div>
      <div class="stack-note">
        结合馆藏机构业务需求，提供可操作流程、可视化表达与可验证证据链
      </div>
    </div>
  </div>
</template>

<style scoped>
.about-page {
  padding: 16px 20px 24px;
  background:
    radial-gradient(900px 420px at 10% -12%, rgba(14, 116, 144, 0.14), transparent 55%),
    linear-gradient(180deg, #f7fbff 0%, #f5f7f9 100%);
}

.about-hero {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 20px;
  padding: 24px;
  border-radius: 20px;
  border: 1px solid #e2e8f0;
  background: #ffffff;
  box-shadow: 0 20px 40px rgba(15, 23, 42, 0.1);
  margin-bottom: 24px;
}

.hero-kicker {
  display: inline-flex;
  padding: 4px 12px;
  border-radius: 999px;
  background: rgba(212, 162, 72, 0.16);
  color: #92400e;
  font-size: 12px;
  letter-spacing: 0.6px;
  margin-bottom: 12px;
}

.hero-content h1 {
  margin: 0 0 12px;
  font-size: 32px;
  font-family: var(--font-serif);
  color: var(--brand-blue-strong);
}

.hero-content p {
  margin: 0 0 16px;
  color: var(--text-muted);
  line-height: 1.8;
}

.hero-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.hero-tags span {
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(14, 165, 233, 0.12);
  border: 1px solid rgba(14, 165, 233, 0.2);
  font-size: 12px;
  color: #0f766e;
}

.hero-card {
  border-radius: 16px;
  border: 1px solid rgba(15, 23, 42, 0.12);
  padding: 20px;
  background: linear-gradient(140deg, #0b1f2a 0%, #0b4f6c 70%, #1f7a8c 100%);
  color: #fff;
  box-shadow: 0 16px 30px rgba(2, 6, 23, 0.3);
}

.hero-card h3 {
  margin: 0 0 8px;
  font-size: 18px;
}

.hero-card p {
  margin: 0 0 16px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.75);
}

.hero-metrics {
  display: grid;
  gap: 12px;
}

.hero-metrics span {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.7);
}

.hero-metrics strong {
  display: block;
  font-size: 18px;
  margin-top: 2px;
}

.about-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.about-card {
  padding: 18px;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  background: #ffffff;
  box-shadow: 0 14px 26px rgba(15, 23, 42, 0.08);
}

.about-card h3 {
  margin: 0 0 8px;
  font-size: 16px;
  color: var(--brand-blue-strong);
}

.about-card p,
.about-card li {
  margin: 0;
  font-size: 13px;
  color: var(--text-muted);
  line-height: 1.7;
}

.about-card ul {
  margin: 0;
  padding-left: 18px;
}

.about-section {
  margin-bottom: 24px;
}

.section-header {
  text-align: center;
  display: grid;
  justify-items: center;
  gap: 6px;
  margin-bottom: 20px;
}

.section-header h2 {
  margin: 0;
  font-size: 24px;
  font-family: var(--font-serif);
  color: var(--brand-blue-strong);
}

.section-header p {
  margin: 0;
  font-size: 14px;
  color: var(--text-muted);
}

.section-header::after {
  content: '';
  width: 50px;
  height: 4px;
  border-radius: 999px;
  background: linear-gradient(90deg, rgba(14, 165, 233, 0.2), rgba(212, 162, 72, 0.8));
}

.highlight-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.highlight-card {
  padding: 18px;
  border-radius: 16px;
  border: 1px solid rgba(15, 23, 42, 0.12);
  background: #ffffff;
  box-shadow: 0 14px 26px rgba(15, 23, 42, 0.08);
}

.highlight-card h4 {
  margin: 0 0 8px;
  font-size: 15px;
  color: var(--brand-blue-strong);
}

.highlight-card p {
  margin: 0;
  font-size: 13px;
  color: var(--text-muted);
  line-height: 1.7;
}

.stack-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
}

.stack-tags span {
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 12px;
  border: 1px solid rgba(14, 165, 233, 0.2);
  background: rgba(14, 165, 233, 0.08);
  color: #0f766e;
}

.stack-note {
  margin-top: 14px;
  text-align: center;
  font-size: 12px;
  color: var(--text-subtle);
}

@media (max-width: 991px) {
  .about-hero {
    grid-template-columns: 1fr;
  }

  .about-grid,
  .highlight-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 767px) {
  .about-page {
    padding: 12px;
  }
}
</style>
