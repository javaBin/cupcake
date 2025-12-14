<script setup lang="ts">
import type { Conference } from "@/types/conference"

const route = useRoute()
const { findConference } = useConferences()

const conferenceId = String(route.params.conferenceId)

const { conferencesFetch, sessionsFetch, ready } =
  useConferenceSessionsData(conferenceId)

const conference = computed<Conference | undefined>(() => {
  if (conferencesFetch.status.value !== "success") return undefined
  return findConference(conferenceId, conferencesFetch.data.value)
})
</script>

<template>
  <div v-if="ready" class="mx-2">
    <SessionsBrowser
      :sessions="sessionsFetch.data.value ?? []"
      :conference="conference"
    />
  </div>

  <UProgress v-else indeterminate class="w-full" />
</template>
