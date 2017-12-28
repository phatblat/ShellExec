/**
 * build.gradle.kts
 * SimpleExec
 */

import com.jfrog.bintray.gradle.BintrayExtension
import java.util.Date
import org.gradle.api.tasks.wrapper.Wrapper.DistributionType
import org.gradle.kotlin.dsl.*
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
        jcenter()
    }

    val kotlinVersion: String by extra
    val junitPlatformVersion: String by extra
    dependencies {
        classpath(kotlin("gradle-plugin", kotlinVersion))
        classpath("org.junit.platform:junit-platform-gradle-plugin:$junitPlatformVersion")
        classpath("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.0")
    }
}

plugins {
    jacoco
    `java-gradle-plugin`
    `kotlin-dsl` // 0.11.1
}

apply {
    plugin("org.junit.platform.gradle.plugin") // org.junit.platform:junit-platform-gradle-plugin
    plugin("com.jfrog.bintray")
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

// https://docs.gradle.org/current/userguide/jacoco_plugin.html#sec:jacoco_getting_started
jacoco {
    toolVersion = "0.7.9"
    reportsDir = file("$buildDir/reports/jacoco")
}

tasks.withType<JacocoReport> {
    reports {
        sourceDirectories = fileTree("src/main/kotlin")
        classDirectories = fileTree("$buildDir/classes/kotlin/main")

        xml.apply {
            isEnabled = true
            destination = File("$buildDir/reports/jacoco.xml")
        }
        csv.apply {
            isEnabled = false
        }
        html.apply {
            destination = File("${buildDir}/jacocoHtml")
        }

        executionData(tasks.withType<Test>())
    }
}

val codeCoverageReport by tasks.creating(JacocoReport::class) {
    dependsOn("test")
//    sourceSets(project.sourceSets.main)
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

gradlePlugin.plugins.create("simple-exec") {
    id = artifactName
    implementationClass = "$javaPackage.$pluginClass"
}

bintray {
    user = property("bintray.user")
    key = property("bintray.api.key")
    setPublications("mavenJava")
    setConfigurations("archives")
    dryRun = true
    publish = true
    pkg.apply {
        repo = "SimpleExec"
        name = "SimpleExec"
        desc = "Gradle plugin with a simpler Exec task."
        websiteUrl = "https://github.com/phatblat/SimpleExec"
        issueTrackerUrl = "https://github.com/phatblat/SimpleExec/issues"
        vcsUrl = "https://github.com/phatblat/SimpleExec.git"
        setLicenses("Apache-2.0")
        setLabels("gradle", "plugin", "exec", "shell", "bash")
        publicDownloadNumbers = true
        version.apply {
            name = project.version.toString()
            desc = "SimpleExec Gradle Plugin ${project.version}"
            released = Date().toString()
            vcsTag = project.version.toString()
            attributes = mapOf("gradle-plugin" to "${project.group}:com.use.less.gradle:gradle-useless-plugin")

            mavenCentralSync.apply {
                sync = false //Optional (true by default). Determines whether to sync the version to Maven Central.
                user = "userToken" //OSS user token
                password = "password" //OSS user password
                close = "1" //Optional property. By default the staging repository is closed and artifacts are released to Maven Central. You can optionally turn this behaviour off (by puting 0 as value) and release the version manually.
            }
        }
    }
}

val deploy by tasks.creating(DefaultTask::class) {
    description = "Deploys the artifact."
    group = "Deployment"
    dependsOn("bintrayUpload")
}

/* -------------------------------------------------------------------------- */
// DSL
/* -------------------------------------------------------------------------- */

/** Retrieves property by key. Useful when properties contain dots. */
fun property(name: String) = properties[name] as String

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

/**
 * Retrieves the [bintray][com.jfrog.bintray.gradle.BintrayExtension] project extension.
 */
val Project.`bintray`: BintrayExtension get() =
    extensions.getByType(BintrayExtension::class.java)

/**
 * Configures the [bintray][com.jfrog.bintray.gradle.BintrayExtension] project extension.
 */
fun Project.`bintray`(configure: BintrayExtension.() -> Unit) =
        extensions.configure(BintrayExtension::class.java, configure)
