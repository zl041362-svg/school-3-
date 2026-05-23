export function readJsonStorage(key, fallback = null) {
  const value = localStorage.getItem(key)
  if (!value) {
    return cloneFallback(fallback)
  }

  try {
    return JSON.parse(value)
  } catch {
    return cloneFallback(fallback)
  }
}

export function writeJsonStorage(key, value) {
  localStorage.setItem(key, JSON.stringify(value))
}

function cloneFallback(fallback) {
  if (typeof fallback === 'function') {
    return fallback()
  }

  return fallback == null ? fallback : structuredClone(fallback)
}
