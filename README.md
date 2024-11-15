# Cupcake

A minimal server providing a read-only version of [cake](https://github.com/javaBin/cake-redux) for javaBin regions to
be able to find possible speakers for local talks.

For frontend - see [frosting](https://github.com/javaBin/frosting)

## TODO

Always lots to do - but - before we can release this:

* Authentication/authorization - finish backend auth to provide JWT based on slack response (and configure a "
  no-auth-local-running" option)
* Some tests would be nice :)

## Build

Gradle application using ktor.

## Local running

You will require env variables:

    SP_BASE_URL=https://sleepingpill.javazone.no
    SP_USER=<username>
    SP_PASSWORD=<password>
    BRING_API_KEY=<key>
    BRING_API_USER=<e-mail>
    SLACK_CLIENT=<client ID>
    SLACK_SECRET=<client secret>
    SLACK_BOT_TOKEN=<app bot token>
    SLACK_CHANNEL=<channel ID>
    SLACK_CHANNEL_NAME=<channel name>
    SLACK_CALLBACK=<callback URL>
    JWT_SECRET=<jwt secret - defaults to "secret" - should be set to something else via env for deployments>
    JWT_ENABLED=true/false
    JWT_REDIRECT=URL for redirect on successful login - optional - defaults to "/"

Note - if using slack authorization locally (JWT_ENABLED=true) you will have to expose your
localhost [frosting](https://github.com/javaBin/frosting) instance via something like [ngrok](https://ngrok.com/) (free
version is more than good enough) and add your exposed callback URL to the slack app's accepted list of callback URLs.

You must then access the site via the exposed URL.

Slack does **not** support localhost or http protocol.

If you are not running with auth then localhost is fine.

## Deploy
  
docker build -t cupcake:latest .

## Slack authorization

This application requires a slack app that provides two functions:

* OIDC login
* A bot that can check channel membership

This is currently provided via the [javaBinAccess](https://api.slack.com/apps/A0817M6EQF3/general) app.

App manifest:

```json
{
  "display_information": {
    "name": "javaBinAccess",
    "description": "Access bot for javaBin",
    "background_color": "#2121cf"
  },
  "features": {
    "bot_user": {
      "display_name": "javaBinAccess",
      "always_online": true
    }
  },
  "oauth_config": {
    "redirect_urls": [
      "<list of allowed callback URLs>"
    ],
    "scopes": {
      "user": [
        "email",
        "openid",
        "profile"
      ],
      "bot": [
        "channels:read",
        "groups:read",
        "users:read"
      ]
    }
  },
  "settings": {
    "org_deploy_enabled": false,
    "socket_mode_enabled": false,
    "token_rotation_enabled": false
  }
}
```

To find the environment settings required:

- Client ID and Client Secret from the slack app > Settings > Basic Information
- Bot User OAuth Token token from the slack app > Features > OAuth & Permissions
- Channel ID - right click channel - channel details - ID is at the end of the dialog
- Channel Name - just the channel name with leading #

