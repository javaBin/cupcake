<script setup lang="ts">
const route = useRoute()

const {findConference, conferenceTitle, conferenceLink} = useConferences()
const {findSession, duration} = useSessions()

const user = useCookie('user_session', {
  readonly: true
})

const opts: UseFetchOptions = {}

if (user.value !== undefined) {
  opts['headers'] = {
    authorization: `Bearer ${user.value}`,
  }
}

const {
  data: conferences,
  status: conferenceStatus
} = await useFetch<Conference[]>(`/api/conferences`, opts)

const {
  data: sessions,
  status
} = await useLazyFetch<Session[]>(`/api/conferences/${route.params.conferenceId}/sessions`, opts)

const conference = computed(() => {
  if (conferenceStatus.value === "success") {
    return findConference(route.params.conferenceId, conferences.value);
  }

  return undefined;
})

const session = computed(() => {
  if (status.value === "success") {
    const session = findSession(route.params.sessionId, sessions.value)

    return session;
  }

  return undefined
})

const sessionLength = computed(() => {
  return duration(session.value);
})

</script>

<template>
  <v-container>
    <h1 v-if="conference">
      <NuxtLink :to="conferenceLink(conference)">{{ conferenceTitle(conference) }}</NuxtLink>
    </h1>

    <div v-if="session">
      <h2 class="my-2">
        {{ session.title }}
        <span v-if="sessionLength"> ({{ sessionLength }})</span>
      </h2>

      <div>
        <IconFormat :format="session.format" full/>
        <IconStatus :status="session.status" full/>
        <IconLanguage :language="session.language" full/>
      </div>

      <div class="my-2">
        {{ session.abstract }}
      </div>

      <h2>Speakers</h2>

      <div class="d-flex flex-wrap ga-3 my-2 justify-center">
        <v-card v-for="(speaker, idx) in session.speakers" :key="idx" class="w-25">
          <v-card-title>{{ speaker.name }}</v-card-title>

          <v-card-text v-if="speaker.bio">{{ speaker.bio }}</v-card-text>

          <v-card-actions>
            <v-list density="compact" lines="two">
              <v-list-item v-if="speaker.email" title="E-mail" :subtitle="speaker.email" />
              <v-list-item v-if="speaker.postcode" title="Postcode" :subtitle="speaker.postcode" />
              <v-list-item v-if="speaker.location" title="Location" :subtitle="speaker.location" />
              <v-list-item v-if="speaker.city" title="City" :subtitle="speaker.city" />
              <v-list-item v-if="speaker.county" title="County" :subtitle="speaker.county" />
            </v-list>
          </v-card-actions>
        </v-card>
      </div>
    </div>

    <v-progress-circular
        color="primary"
        indeterminate
        class="ma-16"
        v-if="status === 'pending' || conferenceStatus === 'pending'"
    />
  </v-container>
</template>

