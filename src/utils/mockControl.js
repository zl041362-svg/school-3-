const isProd = () => import.meta.env.VITE_APP_ENV === 'production' || import.meta.env.PROD

export function allowMockFallback() {
  return !isProd()
}
