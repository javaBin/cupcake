let frontend_host = 'localhost'

if ('CUPCAKE_FRONTEND' in process.env && process.env.CUPCAKE_FRONTEND !== undefined) {
  frontend_host = process.env.CUPCAKE_FRONTEND
}

// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: '2025-05-15',

  future: {
    compatibilityVersion: 4,
  },
  devtools: { enabled: true },
  modules: ['@nuxt/eslint', '@nuxt/icon', 'vuetify-nuxt-module'],
  vite: {
    server: {
      allowedHosts: ['maximum-whale-singularly.ngrok-free.app', frontend_host],
    },
  },
  icon: {
    serverBundle: {
      collections: ['carbon', 'mdi', 'openmoji'],
    },
  },
  vuetify: {
    vuetifyOptions: {
      theme: {
        defaultTheme: 'dark',
      },
    },
  },
})
