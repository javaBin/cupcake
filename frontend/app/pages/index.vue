<script setup lang="ts">

const {user, fetchUser} = useUser()
await fetchUser()

</script>

<template>
  <div class="space-y-6">
    <UCard class="p-8">
      <div class="grid grid-cols-1 md:grid-cols-2 gap-8 items-center">
        <img
            width="250"
            src="~/assets/marius_duke.svg"
            alt="Duke"
            class="w-full h-auto rounded-lg"
        />

        <div>
          <h1 class="text-4xl font-bold tracking-tight mb-4">Cupcake</h1>

          <p class="text-lg text-gray-600 dark:text-gray-300">
            Access to all the JavaZone talks through the years
          </p>

          <p v-if="user" class="text-lg text-gray-600 dark:text-gray-300">
            Welcome {{ user.preferredUsername.charAt(0).toUpperCase() + user.preferredUsername.slice(1).toLowerCase() }}
          </p>

          <p v-if="user && !user.hasPkomRole" class="mt-4 font-semibold text-red-600 dark:text-red-400">
            You need the program committee role to use this application &mdash; ask in <strong>#pkom</strong> on Slack.
          </p>
        </div>
      </div>
    </UCard>
  </div>
</template>
