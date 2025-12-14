<script setup lang="ts">
const user = useCookie("user_session", {
  readonly: true,
})

const { parseJwt } = useJwt()

const userInfo = ref<User | undefined>(undefined)

if (user.value !== null && user.value !== undefined) {
  userInfo.value = parseJwt(user.value)
}
</script>

<template>
  <div class="space-y-6">
    <UCard class="p-8">
      <div class="grid grid-cols-1 md:grid-cols-2 gap-8 items-center">
        <img
          v-if="userInfo === undefined"
          width="250"
          src="~/assets/marius_duke.svg"
          alt="Duke"
          class="w-full h-auto rounded-lg"
        />
        <img
          v-else
          width="250"
          :src="userInfo.avatar"
          :alt="userInfo.name"
          class="w-full h-auto rounded-lg"
        />

        <div>
          <h1 class="text-4xl font-bold tracking-tight mb-4">Cupcake</h1>

          <p class="text-lg text-gray-600 dark:text-gray-300">
            Access to all the JavaZone talks through the years
          </p>

          <UButton
            v-if="userInfo === undefined"
            size="xl"
            href="/login"
            class="my-6"
            external
          >
            <template #leading>
              <Icon name="logos:slack-icon" />
            </template>
            Sign in with slack
          </UButton>

          <p v-else class="text-lg text-gray-600 dark:text-gray-300">
            Welcome {{ userInfo.name }}
          </p>
        </div>
      </div>
    </UCard>
  </div>
</template>
