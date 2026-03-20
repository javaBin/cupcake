import { completeLoginCallback, isAuthenticated, login } from '~/composables/useAuth'

function hasOidcParams(): boolean {
  const params = new URLSearchParams(window.location.search)
  return params.has('code') && params.has('state')
}

export default defineNuxtPlugin(async () => {
  if (hasOidcParams()) {
    const returnUrl = await completeLoginCallback()
    const url = new URL(returnUrl, window.location.origin)

    if (url.origin !== window.location.origin) {
      window.location.replace(`${window.location.origin}/`)
    } else {
      window.location.replace(url.toString())
    }

    return
  }

  addRouteMiddleware(
    'auth',
    async () => {
      const ok = await isAuthenticated()
      if (!ok) {
        await login()
        return false
      }
    },
    { global: true },
  )
})
