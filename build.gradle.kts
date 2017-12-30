/*
 * build.gradle.kts
 * ShellExec
 */

import build.junitPlatform
import com.jfrog.bintray.gradle.BintrayExtension
import java.util.Date
import org.gradle.api.tasks.wrapper.Wrapper.DistributionType
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.kotlin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.junit.platform.gradle.plugin.EnginesExtension
import org.junit.platform.gradle.plugin.FiltersExtension
import org.junit.platform.gradle.plugin.JUnitPlatformExtension

/* -------------------------------------------------------------------------- */
// Properties
/* -------------------------------------------------------------------------- */

group = "at.phatbl"
version = "1.0.1"

val artifactName = "shellexec"
val javaPackage = "$group.$artifactName"
val pluginClass =  "${name}Plugin"

val kotlinVersion: String by extra
project.logger.lifecycle("kotlinVersion: $kotlinVersion")
val junitPlatformVersion: String by extra
val spekVersion: String by extra

/* -------------------------------------------------------------------------- */
// Build Script
/* -------------------------------------------------------------------------- */

buildscript {
    build.loadExtraPropertiesOf(project)

    val kotlinRepo: String by extra
    val junitPlatformVersion: String by extra

    repositories {
        maven(kotlinRepo)
        jcenter()
    }

    dependencies {
        classpath("org.junit.platform:junit-platform-gradle-plugin:$junitPlatformVersion")
    }
}

plugins {
    // Gradle built-in
    jacoco
    `java-gradle-plugin`
    `maven-publish`

    // Kotlin plugins
    kotlin("jvm")

    // Gradle plugin portal - https://plugins.gradle.org/
    id("com.jfrog.bintray") //version "1.8.0"
}

apply {
    // org.junit.platform:junit-platform-gradle-plugin doesn't support new plugin style
    plugin("org.junit.platform.gradle.plugin")
}

val removeBatchFile by tasks.creating(Delete::class) { delete("gradlew.bat") }

tasks {
    "wrapper"(Wrapper::class) {
        gradleVersion = "4.4.1" // kotlin-dsl 0.13.2
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
    compile(kotlin("stdlib", kotlinVersion))
    compile("org.apache.commons:commons-exec:1.3")

    // Speck
    compile(kotlin("reflect", kotlinVersion))
    testCompile(kotlin("test", kotlinVersion))
    testCompile(kotlin("test-junit", kotlinVersion))
    testCompile("org.jetbrains.spek:spek-api:$spekVersion")
    testCompile("org.jetbrains.spek:spek-junit-platform-engine:$spekVersion")
    testCompile("org.junit.platform:junit-platform-runner:$junitPlatformVersion")
}

// java
configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = sourceCompatibility
}

tasks.withType<KotlinCompile> { kotlinOptions.jvmTarget = sourceCompatibility }

// Include resources
java.sourceSets["main"].resources {
    setSrcDirs(mutableListOf("src/main/resources"))
    include("VERSION.txt")
}

val updateVersionFile by tasks.creating {
    description = "Updates the VERSION.txt file included with the plugin"
    group = "Build"
    doLast {
        val versionFile = project.file("src/main/resources/VERSION.txt").writeText(version.toString())
    }
}

tasks.getByName("assemble").dependsOn(updateVersionFile)

val sourcesJar by tasks.creating(Jar::class) {
    dependsOn("classes")
    classifier = "sources"
    from(java.sourceSets["main"].allSource)
}

val javadocJar by tasks.creating(Jar::class) {
    dependsOn("javadoc")
    classifier = "javadoc"
    val javadoc = tasks.withType<Javadoc>().first()
    from(javadoc.destinationDir)
}

artifacts.add("archives", sourcesJar)
artifacts.add("archives", javadocJar)

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
            destination = File("$buildDir/jacocoHtml")
        }

        executionData(tasks.withType<Test>())
    }
}

val codeCoverageReport by tasks.creating(JacocoReport::class) {
    dependsOn("test")
    sourceSets(java.sourceSets["main"])
}

/* -------------------------------------------------------------------------- */
// Deployment
/* -------------------------------------------------------------------------- */

configure<BasePluginConvention> {
    // at.phatbl.shellexec-1.0.0.jar
    archivesBaseName = javaPackage
}

gradlePlugin.plugins.create(artifactName) {
    id = artifactName
    implementationClass = "$javaPackage.$pluginClass"
}

publishing {
    (publications) {
        "mavenJava"(MavenPublication::class) {
            from(components["java"])
            artifactId = artifactName

            artifact(sourcesJar) { classifier = "sources" }
            artifact(javadocJar) { classifier = "javadoc" }
        }
    }
}

bintray {
    user = property("bintray.user") as String
    key = property("bintray.api.key") as String
    setPublications("mavenJava")
    setConfigurations("archives")
    dryRun = false
    publish = true
    pkg.apply {
        repo = "maven-open-source"
        name = "ShellExec"
        desc = "Gradle plugin with a simpler Exec task."
        websiteUrl = "https://github.com/phatblat/ShellExec"
        issueTrackerUrl = "https://github.com/phatblat/ShellExec/issues"
        vcsUrl = "https://github.com/phatblat/ShellExec.git"
        setLicenses("MIT")
        setLabels("gradle", "plugin", "exec", "shell", "bash")
        publicDownloadNumbers = true
        version.apply {
            name = project.version.toString()
            desc = "ShellExec Gradle Plugin ${project.version}"
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

val deploy by tasks.creating {
    description = "Deploys the artifact."
    group = "Deployment"
    dependsOn("bintrayUpload")
}
