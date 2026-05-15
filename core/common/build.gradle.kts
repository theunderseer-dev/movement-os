plugins {
    alias(libs.plugins.movementos.android.library)
    alias(libs.plugins.movementos.quality)
}

android {
    namespace = "com.theunderseer.movementos.core.common"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
}
