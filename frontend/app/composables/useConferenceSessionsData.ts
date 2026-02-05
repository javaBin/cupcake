import type { Conference } from "@/types/conference"
import type { Session } from "@/types/session"

export function useConferenceSessionsData(conferenceId: string) {
  const conferencesFetch = useAuthFetch<Conference[]>("/api/conferences")
  const sessionsFetch = useAuthLazyFetch<Session[]>(
    `/api/conferences/${conferenceId}/sessions`,
  )

  const pending = computed(
    () =>
      conferencesFetch.status.value === "pending" ||
      sessionsFetch.status.value === "pending",
  )

  const ready = computed(
    () =>
      conferencesFetch.status.value === "success" &&
      sessionsFetch.status.value === "success",
  )

  return {
    conferencesFetch,
    sessionsFetch,
    pending,
    ready,
  }
}
