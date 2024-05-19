/*
 * settings.gradle.kts
 * ShellExec
 */

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "ShellExec"

// Workaround to make the JUnit Platform Gradle Plugin available using the `plugins` DSL
// See: https://github.com/junit-team/junit5/issues/768#issuecomment-330078905
pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}
