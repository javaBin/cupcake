<script setup lang="ts">
import {Icon} from "@iconify/vue";

const user = useCookie('user_session', {
  readonly: true
})

const { parseJwt } = useJwt()

const userInfo = ref<User | undefined>(undefined)

if (user.value !== null && user.value !== undefined) {
  userInfo.value = parseJwt(user.value)
}
</script>

<template>
  <v-container class="my-12">
    <v-banner>
      <template v-slot:prepend>
        <img width="250" src="~/assets/marius_duke.svg" alt="Duke"  v-if="userInfo === undefined"/>
        <img width="250" v-else :src="userInfo.avatar" :alt="userInfo.name"/>
      </template>
      <v-banner-text>
        <h1 class="text-h1">Cupcake</h1>
        <p class="my-6 text-body-1">Access to all the JavaZone talks through the years</p>

        <v-btn href="/login" v-if="userInfo === undefined">
          <template v-slot:prepend>
            <Icon icon="logos:slack-icon"/>
          </template>
          Sign in with slack
        </v-btn>

        <p v-else>Welcome {{ userInfo.name }}</p>

      </v-banner-text>
    </v-banner>

  </v-container>
</template>