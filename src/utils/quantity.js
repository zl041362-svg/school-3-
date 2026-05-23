export function normalizeQty(value, stock) {
  const parsed = Number.parseInt(value, 10)
  if (!Number.isFinite(parsed) || parsed < 1) {
    return 1
  }

  if (typeof stock === 'number' && stock > 0) {
    return Math.min(parsed, stock)
  }

  return parsed
}
