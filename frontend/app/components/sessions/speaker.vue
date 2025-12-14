<script setup lang="ts">
import type { Speaker } from "@/types/speaker"

defineProps<{
  speaker: Speaker
}>()
</script>

<template>
  <UCard>
    <template #header>
      <div class="font-semibold">
        {{ speaker.name }}
      </div>
    </template>

    <div v-if="speaker.bio" class="text-sm whitespace-pre-wrap">
      {{ speaker.bio }}
    </div>

    <div class="mt-4 space-y-2 text-sm">
      <div v-if="speaker.email" class="flex gap-2">
        <span class="text-muted w-24">E-mail</span>
        <span class="break-all">{{ speaker.email }}</span>
      </div>

      <div v-if="speaker.postcode" class="flex gap-2">
        <span class="text-muted w-24">Postcode</span>
        <span>{{ speaker.postcode }}</span>
      </div>

      <div v-if="speaker.location" class="flex gap-2">
        <span class="text-muted w-24">Location</span>
        <span class="break-words">{{ speaker.location }}</span>
      </div>

      <div v-if="speaker.city" class="flex gap-2">
        <span class="text-muted w-24">City</span>
        <span class="break-words">{{ speaker.city }}</span>
      </div>

      <div v-if="speaker.county" class="flex gap-2">
        <span class="text-muted w-24">County</span>
        <span class="break-words">{{ speaker.county }}</span>
      </div>
    </div>
  </UCard>
</template>
