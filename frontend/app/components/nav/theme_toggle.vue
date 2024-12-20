<script setup lang="ts">
import {useTheme} from 'vuetify'
import {Icon} from "@iconify/vue";

const theme = useTheme()

function toggleTheme() {
  theme.global.name.value = theme.global.current.value.dark ? 'light' : 'dark'
}
</script>

<template>
  <v-btn @click="toggleTheme">
    <Icon icon="mdi:theme-light-dark"/>
 </v-btn>
</template>
