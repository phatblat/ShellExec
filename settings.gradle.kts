/*
 * settings.gradle.kts
 * ShellExec
 */

rootProject.name = "shellexec"

// Workaround to make the JUnit Platform Gradle Plugin available using the `plugins` DSL
// See: https://github.com/junit-team/junit5/issues/768#issuecomment-330078905
pluginManagement {
    repositories {
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "at.phatbl.shellexec" ->
                    useModule("gradle.plugin.at.phatbl:shellexec:${requested.version}")
                "org.junit.platform.gradle.plugin" ->
                    useModule("org.junit.platform:junit-platform-gradle-plugin:${requested.version}")
                else -> println("")
            }
        }
    }
}
