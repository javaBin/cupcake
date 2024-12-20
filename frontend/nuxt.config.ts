const routeRulesProxy = () => {
    let host = 'https://cupcake-backend.java.no' // TBD

    if (process.env.NODE_ENV === 'development') {
        host = 'http://127.0.0.1:8080'
    }

    return {
        '/api/**': {
            proxy: {
                to: `${host}/api/**`
            }
        },
        '/login': {
            proxy: {
                to: `${host}/login`,
                fetchOptions: {redirect: 'manual'}
            }
        },
        '/slackCallback': {
            proxy: {
                to: `${host}/slackCallback`,
                fetchOptions: {redirect: 'manual'}
            }
        }
    }
}

// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
    compatibilityDate: '2024-04-03',

    future: {
        compatibilityVersion: 4,
    },
    ssr: false,
    devtools: {enabled: true},
    modules: ['vuetify-nuxt-module'],
    routeRules: routeRulesProxy(),
    vuetify: {
        vuetifyOptions: {
            theme: {
                defaultTheme: 'dark'
            }
        }
    }
})