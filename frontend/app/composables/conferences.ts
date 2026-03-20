import type { Conference } from "@/types/conference"
import { ApiError, apiJson } from "~/composables/useApiFetch"

export const useConferences = () => {
  const conferences = useState<Conference[]>("conferences-data", () => [])
  const conferencesPending = useState<boolean>("conferences-pending", () => false)
  const conferencesError = useState<ApiError | null>("conferences-error", () => null)
  const conferencesLoaded = useState<boolean>("conferences-loaded", () => false)

  const fetchConferences = async (): Promise<void> => {
    if (conferencesLoaded.value || conferencesPending.value) return
    conferencesPending.value = true
    conferencesError.value = null
    try {
      conferences.value = await apiJson<Conference[]>("/api/conferences")
    } catch (e) {
      conferencesError.value = e instanceof ApiError ? e : new ApiError(0, "Unknown error")
      conferences.value = []
    } finally {
      conferencesPending.value = false
      conferencesLoaded.value = true
    }
  }

  const conferenceLink = (conference: Conference): string =>
    `/conference/${conference.id}`

  const findConference = (id: string): Conference | undefined =>
    conferences.value.find((c) => c.id === id)

  const conferenceTitle = (conference?: Conference): string => {
    if (conference !== undefined) return conference.name
    return "Javazone"
  }

  return {
    conferences,
    conferencesPending,
    conferencesError,
    conferencesLoaded,
    fetchConferences,
    conferenceLink,
    conferenceTitle,
    findConference,
  }
}
