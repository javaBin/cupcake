const routeRulesProxy = () => {
    let proxy_prefix = 'https://cupcake-backend.java.no'

    if (process.env.NODE_ENV === 'development') {
        proxy_prefix = 'http://127.0.0.1:8080'
    }

    if ('CUPCAKE_BACKEND' in process.env && process.env.CUPCAKE_BACKEND !== undefined) {
        proxy_prefix = process.env.CUPCAKE_BACKEND
    }

    return {
        '/api/**': {
            proxy: {
                to: `${proxy_prefix}/api/**`
            }
        },
        '/login': {
            proxy: {
                to: `${proxy_prefix}/login`,
                fetchOptions: {redirect: 'manual'}
            }
        },
        '/slackCallback': {
            proxy: {
                to: `${proxy_prefix}/slackCallback`,
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