plugins {
    alias(libs.plugins.movementos.jvm.application)
    alias(libs.plugins.movementos.quality)
    alias(libs.plugins.kotlinSerialization)
}

application {
    mainClass.set("com.theunderseer.movementos.backend.ApplicationKt")
}

dependencies {
    implementation(projects.shared)

    implementation(libs.bundles.ktor.server)
    implementation(libs.ktor.server.config.yaml)
    implementation(libs.bundles.ktor.server)
    implementation(libs.logback.classic)
    implementation(libs.typesafe.config)
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger.slf4j)
    implementation(libs.kermit)

    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(projects.core.testing)
}
