/**
 * build.gradle.kts
 * SimpleExec
 */

import org.gradle.api.tasks.wrapper.Wrapper.DistributionType
import org.gradle.kotlin.dsl.`kotlin-dsl`
import org.gradle.kotlin.dsl.kotlin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.junit.platform.gradle.plugin.EnginesExtension
import org.junit.platform.gradle.plugin.FiltersExtension
import org.junit.platform.gradle.plugin.JUnitPlatformExtension

/* -------------------------------------------------------------------------- */
// Properties
/* -------------------------------------------------------------------------- */

group = "at.phatbl"
version = "0.1.0"

val kotlinVersion: String by extra
println("kotlinVersion: $kotlinVersion")
val junitPlatformVersion: String by extra
val spekVersion: String by extra

/* -------------------------------------------------------------------------- */
// Build Script
/* -------------------------------------------------------------------------- */

buildscript {
    build.loadExtraPropertiesOf(project)

    val kotlinRepo: String by extra
    repositories {
        maven(kotlinRepo)
    }

    val kotlinVersion: String by extra
    val junitPlatformVersion: String by extra
    dependencies {
        classpath(kotlin("gradle-plugin", kotlinVersion))
        classpath("org.junit.platform:junit-platform-gradle-plugin:$junitPlatformVersion")
    }
}

plugins {
    `java-gradle-plugin`
    `kotlin-dsl` // 0.11.1
}

apply {
    plugin("org.junit.platform.gradle.plugin") // org.junit.platform:junit-platform-gradle-plugin
}

val removeBatchFile by tasks.creating(Delete::class) { delete("gradlew.bat") }

tasks {
    "wrapper"(Wrapper::class) {
        gradleVersion = "4.4"
        distributionType = DistributionType.ALL
        finalizedBy(removeBatchFile)
    }
}

/* -------------------------------------------------------------------------- */
// Build Configuration
/* -------------------------------------------------------------------------- */

repositories {
    jcenter()
    maven("https://repo.gradle.org/gradle/repo")
    maven("http://dl.bintray.com/jetbrains/spek")
}

// In this section you declare the dependencies for your production and test code
dependencies {
    compile(gradleKotlinDsl())
    compile(kotlin("stdlib", kotlinVersion))
    compile("org.apache.commons:commons-exec:1.3")

    // Speck
    compile("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    testCompile("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
    testCompile("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
    testCompile("org.jetbrains.spek:spek-api:$spekVersion")
    testCompile("org.jetbrains.spek:spek-junit-platform-engine:$spekVersion")
    testCompile("org.junit.platform:junit-platform-runner:$junitPlatformVersion")
}

// java
configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

/* -------------------------------------------------------------------------- */
// Testing
/* -------------------------------------------------------------------------- */

junitPlatform {
    platformVersion = junitPlatformVersion
    filters {
        includeClassNamePatterns("^.*Tests?$", ".*Spec", ".*Spek")
        engines {
            include("spek", "junit-jupiter", "junit-vintage")
        }
    }
}

/* -------------------------------------------------------------------------- */
// Deployment
/* -------------------------------------------------------------------------- */

val artifactName = "simple-exec"
val javaPackage = "$group.simple_exec"
val pluginClass =  "${name}Plugin"

configure<BasePluginConvention> {
    // at.phatbl.simple-exec-1.0.0.jar
    archivesBaseName = javaPackage
}

gradlePlugin {
    plugins {
        create("simple-exec") {
            id = artifactName
            implementationClass = "$javaPackage.$pluginClass"
        }
    }
}

/* -------------------------------------------------------------------------- */
// Groovy-like DSL
/* -------------------------------------------------------------------------- */

/**
 * Retrieves the [junitPlatform][org.junit.platform.gradle.plugin.JUnitPlatformExtension] project extension.
 */
val Project.`junitPlatform`: JUnitPlatformExtension get() =
    extensions.getByType(JUnitPlatformExtension::class.java)

/**
 * Configures the [junitPlatform][org.junit.platform.gradle.plugin.JUnitPlatformExtension] project extension.
 */
fun Project.`junitPlatform`(configure: JUnitPlatformExtension.() -> Unit) =
    extensions.configure(JUnitPlatformExtension::class.java, configure)

/**
 * Retrieves the [gradlePlugin][org.gradle.plugin.devel.GradlePluginDevelopmentExtension] project extension.
 */
val Project.`gradlePlugin`: GradlePluginDevelopmentExtension get() =
    extensions.getByType(GradlePluginDevelopmentExtension::class.java)

/**
 * Configures the [gradlePlugin][org.gradle.plugin.devel.GradlePluginDevelopmentExtension] project extension.
 */
fun Project.`gradlePlugin`(configure: GradlePluginDevelopmentExtension.() -> Unit) =
        extensions.configure(GradlePluginDevelopmentExtension::class.java, configure)
