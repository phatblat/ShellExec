package build

import org.gradle.api.Project
import org.gradle.api.plugins.ExtraPropertiesExtension

/**
 * https://github.com/gradle/kotlin-dsl/blob/234bbd7fbeedf66794345298a59f5ac6996a6f67/buildSrc/src/main/kotlin/build/ExtraProperties.kt#L7
 */
fun loadExtraPropertiesOf(project: Project) = project.run {
    require(this == rootProject) {
        "Properties should be loaded by the root project only!"
    }
    val kotlinRepo = "https://repo.gradle.org/gradle/repo"
    // FIXME: Load from gradle.properties
    val kotlinVersion = file("kotlin-version.txt").readText().trim()
    extra["kotlinVersion"] = kotlinVersion
    extra["kotlinRepo"] = kotlinRepo
    extra["spekVersion"] = "1.1.5"
    extra["junitPlatformVersion"] = "1.0.0"
}

val Project.kotlinVersion get() = rootProject.extra["kotlinVersion"] as String

val Project.kotlinRepo get() = rootProject.extra["kotlinRepo"] as String

fun Project.futureKotlin(module: String) = "org.jetbrains.kotlin:kotlin-$module:$kotlinVersion"

private
val Project.extra: ExtraPropertiesExtension get() = extensions.extraProperties
