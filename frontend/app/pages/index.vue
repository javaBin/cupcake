<script setup lang="ts">
  const user = useCookie('user_session', {
    readonly: true,
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
      <template #prepend>
        <img v-if="userInfo === undefined" width="250" src="~/assets/marius_duke.svg" alt="Duke" />
        <img v-else width="250" :src="userInfo.avatar" :alt="userInfo.name" />
      </template>
      <v-banner-text>
        <h1 class="text-h1">Cupcake</h1>
        <p class="text-body-1 my-6">Access to all the JavaZone talks through the years</p>

        <v-btn v-if="userInfo === undefined" href="/login">
          <template #prepend>
            <Icon name="logos:slack-icon" />
          </template>
          Sign in with slack
        </v-btn>

        <p v-else>Welcome {{ userInfo.name }}</p>
      </v-banner-text>
    </v-banner>
  </v-container>
</template>
