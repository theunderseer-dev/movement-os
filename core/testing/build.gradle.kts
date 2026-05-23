plugins {
    alias(libs.plugins.movementos.kmm.library)
    alias(libs.plugins.movementos.quality)
}

kotlin {
    androidTarget()
    iosArm64()
    iosSimulatorArm64()
    iosX64()

    sourceSets {
        commonMain.dependencies {
            api(projects.shared)
            api(libs.kotlin.test)
            api(libs.kotlinx.coroutines.test)
            api(libs.turbine)
        }
    }
}

android {
    namespace = "com.theunderseer.movementos.core.testing"
}
