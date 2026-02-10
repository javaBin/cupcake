import type { FetchContext } from "ofetch"

interface FetchCallbackOptions {
  onRequest?: (context: FetchContext) => Promise<void> | void
  onResponseError?: (context: {
    response: { status: number }
  }) => Promise<void> | void
  [key: string]: unknown
}

function withAuth(opts: FetchCallbackOptions = {}): FetchCallbackOptions {
  const { accessToken, isAccessTokenExpiringSoon, refresh } = useAuth()

  return {
    ...opts,
    async onRequest(context: FetchContext) {
      if (import.meta.client && isAccessTokenExpiringSoon()) {
        await refresh()
      }

      const token = accessToken.value
      if (token) {
        context.options.headers = new Headers(context.options.headers)
        context.options.headers.set("Authorization", `Bearer ${token}`)
      }

      if (opts.onRequest) {
        await opts.onRequest(context)
      }
    },
    async onResponseError(context: { response: { status: number } }) {
      if (import.meta.client && context.response.status === 401) {
        await refresh()
      }

      if (opts.onResponseError) {
        await opts.onResponseError(context)
      }
    },
  }
}

export function useAuthFetch<T>(url: string, opts?: FetchCallbackOptions) {
  return useFetch<T>(url, withAuth(opts) as never)
}

export function useAuthLazyFetch<T>(url: string, opts?: FetchCallbackOptions) {
  return useLazyFetch<T>(url, withAuth(opts) as never)
}
