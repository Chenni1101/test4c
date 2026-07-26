import { formatUsageScenes } from './assetUsage'

const REQUEST_TIMEOUT_MS = 12000

const buildPrompt = (assetName, usage) => {
  const scenes = formatUsageScenes(usage.scenes)
  const sceneText = scenes.length
    ? scenes.map((scene) => `${scene.name}:${scene.count}`).join(', ')
    : '暂无'

  return [
    '你是文博数字资产的市场分析顾问。',
    `资产名称: ${assetName}`,
    `总调用次数: ${usage.total || 0}`,
    `场景分布: ${sceneText}`,
    '请结合被查询与被引用场景，输出市场改进建议。'
  ].join('\n')
}

const extractText = (data) => {
  if (!data) return ''
  if (typeof data === 'string') return data
  if (data.analysis) return data.analysis
  if (data.result) return data.result
  if (data.message) return data.message
  if (data.choices?.[0]?.message?.content) return data.choices[0].message.content
  if (data.choices?.[0]?.text) return data.choices[0].text
  return ''
}

const summarizeUsage = (usage) => {
  const scenes = formatUsageScenes(usage.scenes)
  const total = usage.total || 0
  const queryCount = scenes
    .filter((scene) => /查询|检索|验证|校验/.test(scene.name))
    .reduce((sum, scene) => sum + scene.count, 0)
  const referenceCount = Math.max(total - queryCount, 0)
  const topScene = scenes[0]?.name || ''
  return { total, queryCount, referenceCount, topScene, scenes }
}

const buildFallbackAnalysis = (assetName, usage) => {
  const { total, queryCount, referenceCount, topScene, scenes } = summarizeUsage(usage)
  if (!total) {
    return {
      summary: `当前还没有足够的调用数据，建议先通过存证查询与展陈引用积累数据样本。`,
      insights: ['数据样本不足，无法识别高频场景。'],
      suggestions: ['先在导览、展教与传播渠道进行小规模试投放，形成真实调用记录。']
    }
  }

  const sceneHint = topScene ? `最高频场景为「${topScene}」。` : '高频场景尚不明确。'
  return {
    summary: `${assetName}累计被调用 ${total} 次，其中查询 ${queryCount} 次、引用 ${referenceCount} 次。${sceneHint}`,
    insights: [
      queryCount > referenceCount
        ? '查询需求集中，说明用户更关注确权与真实性。'
        : '引用需求更高，说明内容正在被场景化使用。',
      scenes.length > 2
        ? '场景分布较分散，有机会做主题化场景运营。'
        : '场景集中度高，可围绕核心场景深化传播。'
    ],
    suggestions: [
      topScene
        ? `围绕「${topScene}」补充故事线与衍生内容，提升转化。`
        : '补充场景标签与说明，提高检索命中率。',
      '结合查询高频人群设计更清晰的授权路径和二次传播包。',
      '增加与文创、展教合作的内容包，扩大引用场景。'
    ]
  }
}

const requestMarketAnalysis = async ({ assetName, usage }) => {
  const apiUrl = import.meta.env.VITE_AI_API_URL
  const apiKey = import.meta.env.VITE_AI_API_KEY
  const fallback = buildFallbackAnalysis(assetName, usage)

  if (!apiUrl) {
    return { source: 'local', ...fallback }
  }

  const controller = new AbortController()
  const timeoutId = window.setTimeout(() => controller.abort(), REQUEST_TIMEOUT_MS)

  try {
    const response = await fetch(apiUrl, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(apiKey ? { Authorization: `Bearer ${apiKey}` } : {})
      },
      body: JSON.stringify({
        assetName,
        usage,
        prompt: buildPrompt(assetName, usage)
      }),
      signal: controller.signal
    })

    if (!response.ok) {
      return { source: 'local', ...fallback }
    }

    const data = await response.json().catch(() => null)
    const text = extractText(data)
    if (!text) {
      return { source: 'local', ...fallback }
    }

    return {
      source: 'api',
      summary: text,
      insights: Array.isArray(data?.insights) ? data.insights : [],
      suggestions: Array.isArray(data?.suggestions) ? data.suggestions : []
    }
  } catch (error) {
    return { source: 'local', ...fallback }
  } finally {
    window.clearTimeout(timeoutId)
  }
}

export { requestMarketAnalysis }
