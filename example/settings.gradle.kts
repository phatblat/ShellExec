/*
 * settings.gradle
 * ShellExecExample
 */

rootProject.name = "ShellExecExample"

pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

includeBuild("../") {
    dependencySubstitution {
        substitute(module("at.phatbl:shellexec")).using(project(":"))
        substitute(module("gradle.plugin.at.phatbl:shellexec")).using(project(":"))
    }
}
