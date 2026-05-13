plugins {
    alias(libs.plugins.movementos.android.library)
}

android {
    namespace = "com.theunderseer.movementos.core.common"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
}
