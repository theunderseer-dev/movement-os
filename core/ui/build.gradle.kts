plugins {
    alias(libs.plugins.movementos.android.library)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.movementos.quality)
}

android {
    namespace = "com.theunderseer.movementos.core.ui"

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(projects.core.common)

    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose.ui)
    implementation(libs.androidx.lifecycle.runtime.ktx)
}
