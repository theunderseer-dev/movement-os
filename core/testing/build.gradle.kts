plugins {
    alias(libs.plugins.movementos.android.library)
}

android {
    namespace = "com.theunderseer.movementos.core.testing"
}

dependencies {
    implementation(projects.core.common)

    // Test libs as implementation (not testImplementation) because this module
    // is a test utilities module that other modules consume as testImplementation
    implementation(libs.junit)
    implementation(libs.mockk)
    implementation(libs.kotlinx.coroutines.test)
    implementation(libs.turbine)
}