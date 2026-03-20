<script setup lang="ts">
import type { Conference } from "@/types/conference"

const route = useRoute()
const { conferencesPending, fetchConferences, findConference } = useConferences()

const conferenceId = String(route.params.conferenceId)

await fetchConferences()

const conference = computed<Conference | undefined>(() => findConference(conferenceId))
</script>

<template>
  <div v-if="!conferencesPending" class="mx-2">
    <SessionsBrowser
        v-if="conference"
        :conference="conference"
    />
  </div>

  <UProgress v-else indeterminate class="w-full"/>
</template>
