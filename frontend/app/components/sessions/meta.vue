<script setup lang="ts">
import type { Session } from "@/types/session"

defineProps<{
  session: Session
  lengthText?: string
}>()
</script>

<template>
  <div class="flex flex-wrap items-center gap-2">
    <IconFormat :format="session.format" full />
    <IconStatus :status="session.status" full />
    <IconLanguage :language="session.language" full />

    <UBadge v-if="lengthText" variant="subtle">
      {{ lengthText }}
    </UBadge>
  </div>
</template>
