<script setup lang="ts">
  import { Icon } from '@iconify/vue'

  const props = defineProps<{
    title: string
    value?: string
    icon: string
  }>()

  const emit = defineEmits<{
    select: [value: string]
  }>()

  const click = () => {
    emit('select', props.value ?? props.title)
  }
</script>

<template>
  <v-btn class="w-100" @click="click">
    <template #prepend>
      <Icon :icon="props.icon" />
    </template>
    <template #default>
      {{ props.title }}
    </template>
  </v-btn>
</template>
