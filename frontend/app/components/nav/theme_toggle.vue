<script setup lang="ts">
  import { Icon } from '@iconify/vue'
  import { useTheme } from 'vuetify'

  const theme = useTheme()

  function toggleTheme() {
    theme.global.name.value = theme.global.current.value.dark ? 'light' : 'dark'
  }
</script>

<template>
  <v-btn @click="toggleTheme">
    <Icon icon="mdi:theme-light-dark" />
  </v-btn>
</template>
