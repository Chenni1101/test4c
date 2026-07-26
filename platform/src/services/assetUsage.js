const STORAGE_KEY = 'assetUsage.v1'

const defaultUsage = {
  '新艺术葡萄少女花瓶': {
    scenes: {
      '沉浸展柜场景': 12,
      '文创授权合作': 7,
      '存证查询': 9,
      '城市巡展引用': 4
    }
  },
  '韦奇伍德蓝陶双耳瓶': {
    scenes: {
      '数字孪生馆展示': 10,
      '学术论文引用': 5,
      '3D模型查询': 18,
      '存证查询': 6,
      '文创授权合作': 3
    }
  },
  '彩绘巴洛克执壶': {
    scenes: {
      '城市巡展引用': 8,
      '导览讲解脚本': 6,
      '存证查询': 4
    }
  },
  '绿地粉彩中国风茶具': {
    scenes: {
      '文创授权合作': 9,
      '存证查询': 5,
      '展教课程引用': 4
    }
  },
  '白瓷北极熊雕塑': {
    scenes: {
      '沉浸展柜场景': 6,
      '公益传播素材': 4,
      '存证查询': 3
    }
  },
  '粉彩花卉茶壶套组': {
    scenes: {
      '沉浸茶席展示': 7,
      '文创授权合作': 5,
      '存证查询': 4
    }
  }
}

const clone = (data) => JSON.parse(JSON.stringify(data))

const computeTotal = (scenes) => Object.values(scenes || {}).reduce((sum, count) => sum + count, 0)

const normalizeEntry = (entry) => {
  const scenes = { ...(entry?.scenes || {}) }
  return { scenes, total: computeTotal(scenes) }
}

const mergeEntry = (base = {}, stored = {}) => {
  const mergedScenes = { ...(base.scenes || {}) }
  Object.entries(stored.scenes || {}).forEach(([scene, count]) => {
    mergedScenes[scene] = count
  })
  return { scenes: mergedScenes, total: computeTotal(mergedScenes) }
}

let cache = null

const loadUsage = () => {
  if (cache) return cache

  const base = clone(defaultUsage)
  if (typeof window === 'undefined' || !window.localStorage) {
    cache = Object.keys(base).reduce((acc, key) => {
      acc[key] = normalizeEntry(base[key])
      return acc
    }, {})
    return cache
  }

  let stored = {}
  try {
    stored = JSON.parse(window.localStorage.getItem(STORAGE_KEY) || '{}')
  } catch (error) {
    stored = {}
  }

  const merged = {}
  const keys = new Set([...Object.keys(base), ...Object.keys(stored)])
  keys.forEach((key) => {
    if (base[key] && stored[key]) {
      merged[key] = mergeEntry(base[key], stored[key])
      return
    }
    if (stored[key]) {
      merged[key] = normalizeEntry(stored[key])
      return
    }
    merged[key] = normalizeEntry(base[key])
  })

  cache = merged
  return cache
}

const saveUsage = (data) => {
  cache = data
  if (typeof window === 'undefined' || !window.localStorage) return
  try {
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(data))
  } catch (error) {
    // ignore storage errors
  }
}

const getUsageSnapshot = () => clone(loadUsage())

const getUsageForAsset = (assetName) => {
  if (!assetName) return { total: 0, scenes: {} }
  const data = loadUsage()
  return normalizeEntry(data[assetName] || { scenes: {} })
}

const recordUsage = (assetName, scene, increment = 1) => {
  if (!assetName || !scene) return null
  const data = loadUsage()
  const entry = normalizeEntry(data[assetName] || { scenes: {} })
  entry.scenes[scene] = (entry.scenes[scene] || 0) + increment
  entry.total = computeTotal(entry.scenes)
  data[assetName] = entry
  saveUsage(data)
  return entry
}

const formatUsageScenes = (scenes) =>
  Object.entries(scenes || {})
    .map(([name, count]) => ({ name, count }))
    .sort((a, b) => b.count - a.count)

export { getUsageSnapshot, getUsageForAsset, recordUsage, formatUsageScenes }
