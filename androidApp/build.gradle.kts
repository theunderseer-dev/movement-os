import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.movementos.quality)
}

android {
    namespace = "com.theunderseer.movementos"
    compileSdk =
        libs.versions.android.compileSdk
            .get()
            .toInt()

    defaultConfig {
        applicationId = "com.theunderseer.movementos"
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
        targetSdk =
            libs.versions.android.targetSdk
                .get()
                .toInt()
        versionCode = 1
        versionName = "1.0"

        val localProperties =
            Properties().apply {
                val file = rootProject.file("local.properties")
                if (file.exists()) {
                    file.inputStream().use { load(it) }
                }
            }
        buildConfigField("String", "GEMINI_API_KEY", "\"${localProperties.getProperty("GEMINI_API_KEY", "")}\"")
        buildConfigField("String", "ANTHROPIC_API_KEY", "\"${localProperties.getProperty("ANTHROPIC_API_KEY", "")}\"")
        buildConfigField("String", "OPENAI_API_KEY", "\"${localProperties.getProperty("OPENAI_API_KEY", "")}\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(projects.shared)
    implementation(projects.core.designsystem)
    testImplementation(projects.core.testing)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose.ui)
    debugImplementation(libs.compose.uiTooling)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.koin.android)
}
