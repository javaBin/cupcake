import { User, UserManager, WebStorageStateStore, Log } from 'oidc-client-ts'

interface AuthState {
  returnUrl: string
}

function isAuthState(value: unknown): value is AuthState {
  return (
      typeof value === 'object' &&
      value !== null &&
      'returnUrl' in value &&
      typeof (value as { returnUrl?: unknown }).returnUrl === 'string'
  )
}

type Discovery = { token_endpoint: string }

type TokenResponse = {
  access_token: string
  refresh_token?: string
  id_token?: string
  expires_in: number
  token_type: string
}

Log.setLevel(Log.WARN)

let _userManager: UserManager | null = null

function getUserManager(): UserManager {
  if (!_userManager) {
    const { oidcAuthority, oidcClientId } = useRuntimeConfig().public
    const origin = window.location.origin
    _userManager = new UserManager({
      authority: oidcAuthority,
      client_id: oidcClientId,
      redirect_uri: `${origin}/`,
      response_type: 'code',
      scope: 'openid profile email offline_access',
      userStore: new WebStorageStateStore({ store: window.sessionStorage }),
      automaticSilentRenew: false,
      loadUserInfo: false,
    })
  }
  return _userManager
}

let currentUser: User | null = null

export async function getUser(): Promise<User | null> {
  if (currentUser) return currentUser
  currentUser = await getUserManager().getUser()
  return currentUser
}

export async function isAuthenticated(): Promise<boolean> {
  const user = await getUser()
  return !!user && !user.expired
}

export async function login(): Promise<void> {
  const state: AuthState = { returnUrl: window.location.href }
  await getUserManager().signinRedirect({ state })
}

export async function completeLoginCallback(): Promise<string> {
  const user = await getUserManager().signinRedirectCallback()
  currentUser = user

  const st = user.state
  if (isAuthState(st)) return st.returnUrl

  return `${window.location.origin}/`
}

export async function logout(): Promise<void> {
  currentUser = null
  await getUserManager().removeUser()
  await getUserManager().signoutRedirect()
}

let tokenEndpointPromise: Promise<string> | null = null

async function getTokenEndpoint(): Promise<string> {
  if (!tokenEndpointPromise) {
    const { oidcAuthority } = useRuntimeConfig().public
    tokenEndpointPromise = fetch(`${oidcAuthority}/.well-known/openid-configuration`)
        .then(async (r) => {
          if (!r.ok) throw new Error('Failed to fetch OIDC discovery')
          return (await r.json()) as Discovery
        })
        .then((d) => d.token_endpoint)
  }
  return tokenEndpointPromise
}

let refreshInFlight: Promise<string | null> | null = null

export async function ensureAccessToken(skewSeconds = 30): Promise<string | null> {
  const user = await getUser()

  if (!user) {
    await login()
    return null
  }

  const now = Math.floor(Date.now() / 1000)
  const exp = user.expires_at ?? 0

  if (exp > now + skewSeconds && user.access_token) {
    return user.access_token
  }

  if (!user.refresh_token) {
    currentUser = null
    await getUserManager().removeUser()
    await login()
    return null
  }

  if (refreshInFlight) return refreshInFlight

  refreshInFlight = (async () => {
    const tokenEndpoint = await getTokenEndpoint()

    const refreshToken = user.refresh_token
    if (!refreshToken) {
      currentUser = null
      await getUserManager().removeUser()
      await login()
      return null
    }

    const { oidcClientId } = useRuntimeConfig().public

    const body = new URLSearchParams()
    body.set('grant_type', 'refresh_token')
    body.set('client_id', oidcClientId)
    body.set('refresh_token', refreshToken)

    const resp = await fetch(tokenEndpoint, {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body,
    })

    if (!resp.ok) {
      currentUser = null
      await getUserManager().removeUser()
      await login()
      return null
    }

    const tr = (await resp.json()) as TokenResponse

    const now = Math.floor(Date.now() / 1000)
    const expiresAt = now + tr.expires_in

    const updated = new User({
      ...user,
      access_token: tr.access_token,
      refresh_token: tr.refresh_token ?? user.refresh_token,
      id_token: tr.id_token ?? user.id_token,
      token_type: tr.token_type,
      expires_at: expiresAt,
    })

    await getUserManager().storeUser(updated)
    currentUser = updated
    return updated.access_token
  })()

  try {
    return await refreshInFlight
  } finally {
    refreshInFlight = null
  }
}
