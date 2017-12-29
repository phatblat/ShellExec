package build

import com.jfrog.bintray.gradle.BintrayExtension
import org.gradle.api.Project
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension
import org.junit.platform.gradle.plugin.JUnitPlatformExtension

/** Retrieves property by key. Useful when properties contain dots. */
//fun Project.property(name: String) = properties[name] as String

/**
 * Retrieves the [junitPlatform][org.junit.platform.gradle.plugin.JUnitPlatformExtension] project extension.
 */
val Project.junitPlatform: JUnitPlatformExtension get() =
    extensions.getByType(JUnitPlatformExtension::class.java)

/**
 * Configures the [junitPlatform][org.junit.platform.gradle.plugin.JUnitPlatformExtension] project extension.
 */
fun Project.junitPlatform(configure: JUnitPlatformExtension.() -> Unit) =
        extensions.configure(JUnitPlatformExtension::class.java, configure)

/**
 * Retrieves the [gradlePlugin][org.gradle.plugin.devel.GradlePluginDevelopmentExtension] project extension.
 */
val Project.gradlePlugin: GradlePluginDevelopmentExtension
    get() =
        extensions.getByType(GradlePluginDevelopmentExtension::class.java)

/**
 * Configures the [gradlePlugin][org.gradle.plugin.devel.GradlePluginDevelopmentExtension] project extension.
 */
fun Project.gradlePlugin(configure: GradlePluginDevelopmentExtension.() -> Unit) =
        extensions.configure(GradlePluginDevelopmentExtension::class.java, configure)

/**
 * Retrieves the [bintray][com.jfrog.bintray.gradle.BintrayExtension] project extension.
 */
val Project.bintray: BintrayExtension get() =
    extensions.getByType(BintrayExtension::class.java)

/**
 * Configures the [bintray][com.jfrog.bintray.gradle.BintrayExtension] project extension.
 */
fun Project.bintray(configure: BintrayExtension.() -> Unit) =
        extensions.configure(BintrayExtension::class.java, configure)
