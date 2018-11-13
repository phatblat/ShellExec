/*
 * build.gradle.kts
 * ShellExec
 */

/* -------------------------------------------------------------------------- */
// 🛃 Imports
/* -------------------------------------------------------------------------- */

import at.phatbl.shellexec.ShellExec
import com.jfrog.bintray.gradle.BintrayExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Delete
import java.util.Date
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.creating
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.kotlin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.junit.platform.console.options.Details
import org.junit.platform.gradle.plugin.EnginesExtension
import org.junit.platform.gradle.plugin.FiltersExtension
import org.junit.platform.gradle.plugin.JUnitPlatformExtension
import java.io.File
import java.nio.file.Files.delete

/* -------------------------------------------------------------------------- */
// 🔌 Plugins
/* -------------------------------------------------------------------------- */

plugins {
    // Gradle built-in
    jacoco
    `java-gradle-plugin`
    maven // only applied to make bintray happy
    `maven-publish`

    // Gradle plugin portal - https://plugins.gradle.org/
    kotlin("jvm") version "1.2.71"
    id("at.phatbl.clamp") version "1.1.0"
    id("at.phatbl.shellexec") version "1.1.3"
    id("com.gradle.plugin-publish") version "0.9.10"
    id("com.jfrog.bintray") version "1.8.0"
    id("io.gitlab.arturbosch.detekt") version "1.0.0.RC6-4"

    // Custom handling in pluginManagement
    id("org.junit.platform.gradle.plugin") version "1.2.0"
}

/* -------------------------------------------------------------------------- */
// 📋 Properties
/* -------------------------------------------------------------------------- */

val artifactName: String by project
val javaPackage = "$group.$artifactName"
val pluginClass: String by project
val projectUrl: String by project
val tags: String by project
val labels = "$tags".split(",")
val license: String by project

val jvmTarget = JavaVersion.VERSION_1_8

val commonsExecVersion: String by project
val spekVersion: String by project
val detektVersion: String by project

// FIXME: Get version from plugins block
// This is necessary to make the plugin version accessible in other places
// https://stackoverflow.com/questions/46053522/how-to-get-ext-variables-into-plugins-block-in-build-gradle-kts/47507441#47507441
//val junitPlatformVersion: String? by extra {
//    buildscript.configurations["classpath"]
//            .resolvedConfiguration.firstLevelModuleDependencies
//            .find { it.moduleName == "junit-platform-gradle-plugin" }?.moduleVersion
//}

val junitPlatformVersion: String by project
val jacocoVersion: String by project

/* -------------------------------------------------------------------------- */
// 👪 Dependencies
/* -------------------------------------------------------------------------- */

repositories.jcenter()

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("org.apache.commons:commons-exec:$commonsExecVersion")

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
    testImplementation("org.junit.platform:junit-platform-runner:$junitPlatformVersion")
    testImplementation("org.jetbrains.spek:spek-api:$spekVersion")
    testImplementation("org.jetbrains.spek:spek-junit-platform-engine:$spekVersion")
}

/* -------------------------------------------------------------------------- */
// 🏗 Assemble
/* -------------------------------------------------------------------------- */

tasks.withType<KotlinCompile> { kotlinOptions.jvmTarget = "$jvmTarget" }

configure<JavaPluginConvention> {
    sourceCompatibility = jvmTarget
    targetCompatibility = jvmTarget
}

// Include resources
sourceSets["main"].resources {
    srcDirs("src/main/resources")
    include("VERSION.txt")
}

val updateVersionFile by tasks.creating {
    description = "Updates the VERSION.txt file included with the plugin"
    group = "Build"
    doLast {
        val resources = "src/main/resources"
        project.file(resources).mkdirs()
        val versionFile = project.file("$resources/VERSION.txt")
        versionFile.createNewFile()
        versionFile.writeText(version.toString())
    }
}

tasks.getByName("assemble").dependsOn(updateVersionFile)

val sourcesJar by tasks.creating(Jar::class) {
    dependsOn("classes")
    classifier = "sources"
    from(sourceSets["main"].allSource)
}

val javadocJar by tasks.creating(Jar::class) {
    dependsOn("javadoc")
    classifier = "javadoc"
    val javadoc = tasks.withType<Javadoc>().first()
    from(javadoc.destinationDir)
}

artifacts.add("archives", sourcesJar)
artifacts.add("archives", javadocJar)

configure<BasePluginConvention> {
    // at.phatbl.shellexec-1.0.0.jar
    archivesBaseName = javaPackage
}

gradlePlugin.plugins.create("$artifactName") {
    id = javaPackage
    implementationClass = "$javaPackage.$pluginClass"
}

pluginBundle {
    website = "$projectUrl"
    vcsUrl = "$projectUrl"
    description = project.description
    tags = labels

    plugins.create("$artifactName") {
        id = javaPackage
        displayName = project.name
    }
    mavenCoordinates.artifactId = "$artifactName"
}

/* -------------------------------------------------------------------------- */
// ✅ Test
/* -------------------------------------------------------------------------- */

junitPlatform {
    filters {
        includeClassNamePatterns("^.*Tests?$", ".*Spec", ".*Spek")
        engines {
            include("spek")
        }
    }
    details = Details.TREE
}

// https://docs.gradle.org/current/userguide/jacoco_plugin.html#sec:jacoco_getting_started
jacoco {
    toolVersion = "$jacocoVersion"
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
            isEnabled = true
            destination = File("$buildDir/jacocoHtml")
        }

        executionData(tasks.withType<Test>())
    }
}

val codeCoverageReport by tasks.creating(JacocoReport::class) {
    dependsOn("test")
    // sourceSets(sourceSets["main"])
}

/* -------------------------------------------------------------------------- */
// 🔍 Code Quality
/* -------------------------------------------------------------------------- */

detekt {
    version = "$detektVersion"
    profile("main", Action {
        input = "$projectDir/src/main/kotlin"
        config = "$projectDir/detekt.yml"
        filters = ".*test.*,.*/resources/.*,.*/tmp/.*"
    })
    idea(Action {
        path = ".idea"
        codeStyleScheme = ".idea/code-style.xml"
        inspectionsProfile = ".idea/inspect.xml"
        report = "$projectDir/reports"
        mask = "*.kt,"
    })
}

val lint by tasks.creating(DefaultTask::class) {
    description = "Runs detekt and validateTaskProperties"
    group = "Verification"
    // Does this task come from java-gradle-plugin?
    dependsOn("validateTaskProperties")
    dependsOn("detektCheck")
}

val danger by tasks.creating(ShellExec::class) {
    description = "Runs danger rules."
    group = "Verification"
    command = """\
        bundle install --gemfile=Gemfile --verbose
        ./bin/danger --verbose"""
}

val codeQuality by tasks.creating(DefaultTask::class) {
    description = "Runs all code quality checks."
    group = "🚇 Tube"
    dependsOn("detektCheck")
    dependsOn("check")
    dependsOn(lint)
}

/* -------------------------------------------------------------------------- */
// Release
/* -------------------------------------------------------------------------- */

val release by tasks.creating(DefaultTask::class) {
    description = "Performs release actions."
    group = "🚇 Tube"
    doLast { logger.lifecycle("Release task not implemented.") }
}

/* -------------------------------------------------------------------------- */
// 🚀 Deployment
/* -------------------------------------------------------------------------- */

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
            artifactId = "$artifactName"

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
        repo = property("bintray.repo") as String
        name = project.name
        desc = project.description
        websiteUrl = "$projectUrl"
        issueTrackerUrl = "$projectUrl/issues"
        vcsUrl = "$projectUrl.git"
        githubRepo = "phatblat/${project.name}"
        githubReleaseNotesFile = "CHANGELOG.md"
        setLicenses(property("license") as String)
        setLabels("gradle", "plugin", "exec", "shell", "bash", "kotlin")
        publicDownloadNumbers = true
        version.apply {
            name = project.version.toString()
            desc = "ShellExec Gradle Plugin ${project.version}"
            released = Date().toString()
            vcsTag = "$project.version"
            attributes = mapOf("gradle-plugin" to "${project.group}:$artifactName:$version")

            mavenCentralSync.apply {
                sync = false //Optional (true by default). Determines whether to sync the version to Maven Central.
                user = "userToken" //OSS user token
                password = "password" //OSS user password
                close = "1" //Optional property. By default the staging repository is closed and artifacts are released to Maven Central. You can optionally turn this behaviour off (by puting 0 as value) and release the version manually.
            }
        }
    }
}

// Workaround to eliminate warning from bintray plugin, which assumes the "maven" plugin is being used.
// https://github.com/bintray/gradle-bintray-plugin/blob/master/src/main/groovy/com/jfrog/bintray/gradle/BintrayPlugin.groovy#L85
val install by tasks
install.doFirst {
    val maven = project.convention.plugins["maven"] as MavenPluginConvention
    maven.mavenPomDir = file("$buildDir/publications/mavenJava")
    logger.info("Configured maven plugin to use same output dir as maven-publish: ${maven.mavenPomDir}")
}

val deploy by tasks.creating {
    description = "Deploys the artifact."
    group = "Deployment"
    dependsOn("bintrayUpload")
    dependsOn("publishPlugins")
}
