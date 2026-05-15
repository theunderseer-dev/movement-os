import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

class QualityConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jlleitschuh.gradle.ktlint")
        pluginManager.apply("io.gitlab.arturbosch.detekt")

        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

        dependencies {
            "detektPlugins"(libs.findLibrary("detekt-formatting").get())
        }

        configure<KtlintExtension> {
            version.set(libs.findVersion("ktlint").get().requiredVersion)
            android.set(true)
            ignoreFailures.set(false)
            reporters {
                reporter(ReporterType.PLAIN)
                reporter(ReporterType.CHECKSTYLE)
                reporter(ReporterType.SARIF)
            }
            filter {
                exclude("**/generated/**")
                exclude("**/build/**")
            }
        }

        configure<DetektExtension> {
            config.setFrom(rootProject.files("config/detekt/detekt.yml"))
            buildUponDefaultConfig = true
            autoCorrect = true
            parallel = true
            // Cover all KMM source sets; detekt silently skips paths that don't exist.
            source.setFrom(
                "src/main/kotlin",
                "src/commonMain/kotlin",
                "src/androidMain/kotlin",
                "src/iosMain/kotlin",
            )
        }

        tasks.withType<Detekt>().configureEach {
            exclude("**/generated/**")
            exclude("**/build/**")
            reports {
                html.required.set(true)
                xml.required.set(true)
                sarif.required.set(true)
                md.required.set(false)
                txt.required.set(false)
            }
        }
    }
}