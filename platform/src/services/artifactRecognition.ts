export type ArtifactRecognition = {
  source: 'API' | 'DEMO_LOCAL'
  artifactType: string
  era: string
  description: string
  keywords: string[]
  notice: string
}

const TIMEOUT_MS = 10_000

function localFallback(file: File): ArtifactRecognition {
  const name = file.name.toLowerCase()
  const artifactType = /瓶|vase|瓷|pottery/.test(name) ? 'image' : 'image'
  const description = `该图像文件「${file.name}」已完成本地演示识别。建议结合馆藏档案、来源证明和人工审核补充文物信息。`
  return { source: 'DEMO_LOCAL', artifactType, era: '待人工核验', description, keywords: ['文博数字资产', '图像素材', '待人工核验'], notice: '本地 Demo 识别结果仅用于辅助填写，不是权威文物鉴定结论。' }
}

function normalize(data: any, fallback: ArtifactRecognition): ArtifactRecognition {
  return {
    source: 'API',
    artifactType: typeof data?.artifactType === 'string' ? data.artifactType : fallback.artifactType,
    era: typeof data?.era === 'string' ? data.era : fallback.era,
    description: typeof data?.description === 'string' ? data.description : fallback.description,
    keywords: Array.isArray(data?.keywords) ? data.keywords.filter((item: unknown) => typeof item === 'string').slice(0, 8) : fallback.keywords,
    notice: 'AI 识别结果仅供辅助参考，不能替代人工鉴定、来源核验或法律意见。'
  }
}

export async function recognizeArtifact(file: File): Promise<ArtifactRecognition> {
  const fallback = localFallback(file)
  const endpoint = import.meta.env.VITE_AI_VISION_API_URL
  if (!endpoint || !file.type.startsWith('image/')) return fallback
  const controller = new AbortController()
  const timer = window.setTimeout(() => controller.abort(), TIMEOUT_MS)
  try {
    const form = new FormData()
    form.append('image', file)
    form.append('filename', file.name)
    const response = await fetch(endpoint, { method: 'POST', body: form, signal: controller.signal })
    if (!response.ok) return fallback
    return normalize(await response.json(), fallback)
  } catch {
    return fallback
  } finally { window.clearTimeout(timer) }
}
