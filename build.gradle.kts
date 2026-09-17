plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinter)
    alias(libs.plugins.detekt)
    alias(libs.plugins.versions)
    alias(libs.plugins.serialization)
    alias(libs.plugins.dependency.analysis)
    alias(libs.plugins.release)
    jacoco
}

group = "no.java.cupcake"

kotlin {
    jvmToolchain(25)

    compilerOptions {
        freeCompilerArgs = listOf("-Xconsistent-data-class-copy-visibility")
    }
}

application {
    mainClass.set("no.java.cupcake.ApplicationKt")
    applicationName = "cupcake"

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

base {
    archivesName = "cupcake"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.bundles.ktor.server)
    implementation(libs.bundles.ktor.client)

    implementation(libs.arrow.core)
    implementation(libs.cache4k)
    implementation(libs.caffeine)
    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.kotlin.logging)
    implementation(libs.micrometer.registry.prometheus)

    runtimeOnly(libs.logback.classic)

    testImplementation(libs.bundles.test)
    testRuntimeOnly(libs.kotlin.test.junit)
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

tasks.check {
    dependsOn(tasks.detektMain, tasks.detektTest)
}

listOf("distTar", "distZip", "shadowJar", "startShadowScripts", "shadowDistTar", "shadowDistZip").forEach { name ->
    tasks.named(name) { enabled = false }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
}
