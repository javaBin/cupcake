export default defineNuxtRouteMiddleware(async () => {
  if (import.meta.server) return

  const { isAuthenticated, isAccessTokenExpiringSoon, refresh } = useAuth()

  if (isAuthenticated.value && isAccessTokenExpiringSoon()) {
    await refresh()
  }
})
