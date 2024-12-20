<script setup lang="ts">
const route = useRoute()

const {findConference, conferenceTitle} = useConferences()
const {sessionLink} = useSessions()

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

const headers = [
  {title: "Speaker", key: "speakers"},
  {title: "Title", key: "title"},
  {title: "Format", key: "format", filterable: false, align: "center"},
  {title: "Status", key: "status", filterable: false, align: "center"},
  {title: "Language", key: "language", filterable: false, align: "center"},
  {title: "Length", key: "length"},
  {title: "Location", key: "location", value: "speakers"},
  {title: "", key: "id", filterable: false, sortable: false, align: "center"}
]

const search = ref('')

const conference = computed(() => {
  if (conferenceStatus.value === "success") {
    return findConference(route.params.conferenceId, conferences.value)
  }
})

const ready = computed(() => {
  return status.value === 'success' && conferenceStatus.value === 'success';
})

const selectedFormat = ref<string | undefined>()
const selectedLanguage = ref<string | undefined>()
const selectedStatus = ref<string | undefined>()
const selectedCity = ref<string | undefined>()
const selectedCounty = ref<string | undefined>()

const selectFormat = ((format: string) => {
  selectedFormat.value = format.toLocaleUpperCase();
})

const selectStatus = ((status: string) => {
  selectedStatus.value = status.toLocaleUpperCase();
})

const selectLanguage = ((language: string) => {
  selectedLanguage.value = language.toLocaleUpperCase();
})

const clearFormat = (() => {
  selectedFormat.value = undefined;
})

const clearStatus = (() => {
  selectedStatus.value = undefined;
})


const clearLanguage = (() => {
  selectedLanguage.value = undefined;
})

const clearLocation = (() => {
  selectedCity.value = undefined
  selectedCounty.value = undefined
})

const filteredSessions = computed(() => {
  return sessions.value?.filter((session) => {
    return selectedFormat.value === undefined || session.format === selectedFormat.value;
  }).filter((session) => {
    return selectedStatus.value === undefined || session.status === selectedStatus.value;
  }).filter((session) => {
    return selectedLanguage.value === undefined || session.language === selectedLanguage.value;
  }).filter((session) => {
    return selectedCity.value === undefined || session.speakers.filter((speaker) => {
      return speaker.city?.toLocaleUpperCase().includes(selectedCity.value?.toLocaleUpperCase())
    }).length > 0
  }).filter((session) => {
    return selectedCounty.value === undefined || session.speakers.filter((speaker) => {
      return speaker.county?.toLocaleUpperCase().includes(selectedCounty.value?.toLocaleUpperCase())
    }).length > 0
  });
})
</script>

<template>
  <v-container>

    <h1 v-if="ready">{{ conferenceTitle(conference) }}</h1>

    <div v-if="ready" class="mx-2">
      <v-expansion-panels>
        <v-expansion-panel>
          <v-expansion-panel-title>
            <b>Filters:</b>
            <IconFormat v-if="selectedFormat" :format="selectedFormat" full/>
            <IconStatus v-if="selectedStatus" :status="selectedStatus" full/>
            <IconLanguage v-if="selectedLanguage" :language="selectedLanguage" full/>
            <span class="mx-2" v-if="selectedCity">City: {{ selectedCity }}</span>
            <span class="mx-2" v-if="selectedCounty">County: {{ selectedCounty }}</span>
          </v-expansion-panel-title>
          <v-expansion-panel-text>
            <div class="d-flex ga-3 justify-center">
              <v-card class="w-100 d-flex flex-column">
                <v-card-title>Format</v-card-title>
                <v-card-text>
                  <div class="d-flex flex-column ga-3">
                    <IconBtn title="Presentation" value="PRESENTATION" icon="carbon:group-presentation" @select="selectFormat"/>
                    <IconBtn title="Lightning Talk" value="LIGHTNING_TALK" icon="carbon:lightning" @select="selectFormat"/>
                    <IconBtn title="Workshop" value="WORKSHOP" icon="carbon:laptop" @select="selectFormat"/>
                    <IconBtn title="Panel Debate" value="PANEL" icon="carbon:forum" @select="selectFormat"/>
                  </div>
                </v-card-text>
                <v-card-actions>
                  <v-btn @click="clearFormat">Clear</v-btn>
                </v-card-actions>
              </v-card>
              <v-card class="w-100 d-flex flex-column">
                <v-card-title>Status</v-card-title>
                <v-card-text>
                  <div class="d-flex flex-column ga-3">
                    <IconBtn title="Approved" icon="carbon:task-approved" @select="selectStatus"/>
                    <IconBtn title="Submitted" icon="carbon:task-view" @select="selectStatus"/>
                    <IconBtn title="Rejected" icon="carbon:task-remove" @select="selectStatus"/>
                    <IconBtn title="Historic" icon="carbon:task-asset-view" @select="selectStatus"/>
                    <IconBtn title="Draft" icon="carbon:task-add" @select="selectStatus"/>
                  </div>
                </v-card-text>
                <v-card-actions>
                  <v-btn @click="clearStatus">Clear</v-btn>
                </v-card-actions>
              </v-card>
              <v-card class="w-100 d-flex flex-column">
                <v-card-title>Language</v-card-title>
                <v-card-text>
                  <div class="d-flex flex-column ga-3">
                    <IconBtn title="English" icon="openmoji:flag-united-kingdom" @select="selectLanguage"/>
                    <IconBtn title="Norwegian" icon="openmoji:flag-norway" @select="selectLanguage"/>
                  </div>
                </v-card-text>
                <v-card-actions>
                  <v-btn @click="clearLanguage">Clear</v-btn>
                </v-card-actions>
              </v-card>
              <v-card class="w-100 d-flex flex-column">
                <v-card-title>Location</v-card-title>
                <v-card-text>
                  <v-text-field label="City" v-model="selectedCity"></v-text-field>
                  <v-text-field label="County" v-model="selectedCounty"></v-text-field>
                </v-card-text>
                <v-card-actions>
                  <v-btn @click="clearLocation">Clear</v-btn>
                </v-card-actions>
              </v-card>
            </div>
          </v-expansion-panel-text>
        </v-expansion-panel>
      </v-expansion-panels>
    </div>

    <v-text-field
        v-if="ready"
        v-model="search"
        label="Search"
        prepend-inner-icon="mdi-magnify"
        variant="outlined"
        hide-details
        single-line
        class="ma-2"
    ></v-text-field>

    <v-progress-circular
        color="primary"
        indeterminate
        class="ma-16"
        v-if="!ready"
    />

    <v-data-table
        v-if="ready"
        :items="filteredSessions"
        :headers="headers"
        density="comfortable"
        :search="search"
        items-per-page="100">
      <template v-slot:[`item.speakers`]="{ value }">
        <v-list lines="two" density="comfortable">
          <v-list-item v-for="(speaker, index) in value" :idx="index" :title="speaker.name" :subtitle="speaker.email"/>
        </v-list>
      </template>
      <template v-slot:[`item.location`]="{ value }">
        <v-list lines="two" density="comfortable">
          <v-list-item v-for="(speaker, index) in value" :idx="index" :title="speaker.postcode">
            <v-list-item-subtitle>
              <span class="mx-2" v-if="speaker.location">{{ speaker.location }}</span>
              <span class="mx-2" v-if="speaker.city">{{ speaker.city }}</span>
              <span class="mx-2" v-if="speaker.county">{{ speaker.county }}</span>
            </v-list-item-subtitle>
          </v-list-item>

        </v-list>
      </template>
      <template v-slot:[`item.id`]="{ value }">
        <v-btn v-if="value" :to="sessionLink(conference, value)">
          <v-icon>mdi-file-document-outline</v-icon>
        </v-btn>
      </template>
      <template v-slot:[`item.status`]="{ value }">
        <IconStatus :status="value" full/>
      </template>
      <template v-slot:[`item.format`]="{ value }">
        <IconFormat :format="value" full/>
      </template>
      <template v-slot:[`item.language`]="{ value }">
        <IconLanguage :language="value" full/>
      </template>
    </v-data-table>
  </v-container>
</template>

<style scoped>
.filter {
  flex-direction: column;
}
</style>