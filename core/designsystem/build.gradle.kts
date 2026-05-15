plugins {
    alias(libs.plugins.movementos.android.library)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.movementos.quality)
}

android {
    namespace = "com.theunderseer.movementos.core.designsystem"

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose.ui)
    debugImplementation(libs.compose.uiTooling)
}
