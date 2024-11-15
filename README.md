# Cupcake

A minimal server providing a read-only version of [cake](https://github.com/javaBin/cake-redux) for javaBin regions to
be able to find possible speakers for local talks.

For frontend - see [frosting](https://github.com/javaBin/frosting)

## TODO

Always lots to do - but - before we can release this:

* Require frontend to authenticate - currently [Security.kt](src/main/kotlin/no/java/cupcake/plugins/Security.kt) is just placeholder code.
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

## Deploy

docker build -t cupcake:latest .

