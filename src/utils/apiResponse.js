export function resolveItems(result, fallback = []) {
  return result?.items || result?.list || result?.data || fallback
}

export function resolveEntity(result) {
  return result?.order || result?.data || result
}

export function resolvePagination(result, defaults = {}) {
  return {
    total: result?.total || defaults.total || 0,
    page: result?.page || defaults.page || 1,
    pageSize: result?.pageSize || defaults.pageSize || 20,
  }
}
