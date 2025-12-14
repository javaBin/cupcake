<script setup lang="ts">
import type { Conference } from "@/types/conference"
import type { Session } from "@/types/session"

const route = useRoute()

const { findConference, conferenceTitle, conferenceLink } = useConferences()
const { findSession, duration } = useSessions()

const conferenceId = String(route.params.conferenceId)
const sessionId = String(route.params.sessionId)

const { conferencesFetch, sessionsFetch, pending } =
  useConferenceSessionsData(conferenceId)

const conference = computed<Conference | undefined>(() => {
  if (conferencesFetch.status.value !== "success") return undefined
  return findConference(conferenceId, conferencesFetch.data.value)
})

const session = computed<Session | undefined>(() => {
  if (sessionsFetch.status.value !== "success") return undefined
  return findSession(sessionId, sessionsFetch.data.value)
})

const sessionLength = computed(() => duration(session.value))
</script>

<template>
  <div class="mx-2 space-y-4">
    <div v-if="conference" class="text-xl font-semibold">
      <NuxtLink :to="conferenceLink(conference)" class="hover:underline">
        {{ conferenceTitle(conference) }}
      </NuxtLink>
    </div>

    <div v-if="session" class="space-y-4">
      <UCard>
        <template #header>
          <div class="space-y-2">
            <div class="text-2xl font-semibold leading-snug break-words">
              {{ session.title }}
            </div>

            <SessionsMeta :session="session" :length-text="sessionLength" />
          </div>
        </template>

        <div class="whitespace-pre-wrap break-words">
          {{ session.abstract }}
        </div>
      </UCard>

      <div class="text-xl font-semibold">Speakers</div>

      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
        <SessionsSpeaker
          v-for="(speaker, idx) in session.speakers"
          :key="idx"
          :speaker="speaker"
        />
      </div>
    </div>

    <UProgress v-if="pending" indeterminate class="w-full" />
  </div>
</template>
