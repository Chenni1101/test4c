const STORAGE_KEY = 'dynamicRights.v1'

const defaultState = {
  heatCoefficient: 1.5,
  suggestedRate: 0.03,
  highlightType: '3d',
  active: false,
  lastSignal: '',
  updatedAt: ''
}

const clone = (data) => JSON.parse(JSON.stringify(data))

const loadState = () => {
  if (typeof window === 'undefined' || !window.localStorage) {
    return clone(defaultState)
  }

  let stored = {}
  try {
    stored = JSON.parse(window.localStorage.getItem(STORAGE_KEY) || '{}')
  } catch (error) {
    stored = {}
  }

  return {
    ...defaultState,
    ...stored
  }
}

const saveState = (state) => {
  if (typeof window === 'undefined' || !window.localStorage) return
  try {
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(state))
  } catch (error) {
    // ignore storage errors
  }
}

const getDynamicRightsState = () => clone(loadState())

const setDynamicRightsSuggestion = ({ highlightType, suggestedRate, heatCoefficient, reason } = {}) => {
  const current = loadState()
  const nextState = {
    ...current,
    active: true,
    highlightType: highlightType || current.highlightType,
    suggestedRate: typeof suggestedRate === 'number' ? suggestedRate : current.suggestedRate,
    heatCoefficient: typeof heatCoefficient === 'number' ? heatCoefficient : current.heatCoefficient,
    lastSignal: reason || current.lastSignal,
    updatedAt: new Date().toISOString()
  }

  saveState(nextState)
  return clone(nextState)
}

export { getDynamicRightsState, setDynamicRightsSuggestion }
