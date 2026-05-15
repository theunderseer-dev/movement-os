plugins {
    `kotlin-dsl`
}

group = "com.theunderseer.movementos.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.detekt.gradle.plugin)
    compileOnly(libs.ktlint.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("androidLibrary") {
            id = "movementos.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidFeature") {
            id = "movementos.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("androidApplication") {
            id = "movementos.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("quality") {
            id = "movementos.quality"
            implementationClass = "QualityConventionPlugin"
        }
    }
}
