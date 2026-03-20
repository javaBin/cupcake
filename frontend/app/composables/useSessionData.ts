import type { Session } from "@/types/session"
import { ApiError, apiJson } from "~/composables/useApiFetch"

export const useSessionData = (conferenceId: string) => {
  const sessions = useState<Session[]>(`sessions-data-${conferenceId}`, () => [])
  const sessionsPending = useState<boolean>(`sessions-pending-${conferenceId}`, () => false)
  const sessionsError = useState<ApiError | null>(`sessions-error-${conferenceId}`, () => null)
  const sessionsLoaded = useState<boolean>(`sessions-loaded-${conferenceId}`, () => false)

  const fetchSessions = async (): Promise<void> => {
    if (sessionsLoaded.value || sessionsPending.value) return
    sessionsPending.value = true
    sessionsError.value = null
    try {
      sessions.value = await apiJson<Session[]>(`/api/conferences/${conferenceId}/sessions`)
    } catch (e) {
      sessionsError.value = e instanceof ApiError ? e : new ApiError(0, "Unknown error")
      sessions.value = []
    } finally {
      sessionsPending.value = false
      sessionsLoaded.value = true
    }
  }

  return { sessions, sessionsPending, sessionsError, sessionsLoaded, fetchSessions }
}
