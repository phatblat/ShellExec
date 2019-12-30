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
    kotlin("jvm") version "1.3.60"
    id("at.phatbl.shellexec") version "1.4.1"
    id("com.gradle.plugin-publish") version "0.10.1"
    id("com.jfrog.bintray") version "1.8.4"
    id("io.gitlab.arturbosch.detekt") version "1.2.2"

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
val labels = tags.split(",")
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
val gradleWrapperVersion: String by project

tasks.wrapper {
    gradleVersion = gradleWrapperVersion
    distributionType = Wrapper.DistributionType.ALL
}

/* -------------------------------------------------------------------------- */
// 👪 Dependencies
/* -------------------------------------------------------------------------- */

repositories.jcenter()

dependencies {
    api("org.apache.commons:commons-exec:$commonsExecVersion")
    implementation(kotlin("stdlib"))
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

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

tasks.getByName("processResources").dependsOn(updateVersionFile)
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
    archivesBaseName = artifactName
}

gradlePlugin.plugins.create(artifactName) {
    id = javaPackage
    implementationClass = "$javaPackage.$pluginClass"
}

pluginBundle {
    website = projectUrl
    vcsUrl = projectUrl
    description = project.description

    (plugins) {
        artifactName {
            id = javaPackage
            displayName = project.name
            tags = labels
            version = project.version.toString()
        }
    }
    mavenCoordinates.artifactId = artifactName
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
    toolVersion = jacocoVersion
    reportsDir = file("$buildDir/reports/jacoco")
}

tasks.jacocoTestReport {
    reports {
        xml.isEnabled = true
        html.destination = file("$buildDir/reports/jacoco.xml")
        csv.isEnabled = false
        html.isEnabled = true
        html.destination = file("$buildDir/reports/jacocoHtml")
    }
}

val codeCoverageReport by tasks.creating(JacocoReport::class) {
    dependsOn("test")
    // sourceSets(sourceSets["main"])
}

/* -------------------------------------------------------------------------- */
// 🔍 Code Quality
/* -------------------------------------------------------------------------- */

// https://arturbosch.github.io/detekt/kotlindsl.html
detekt {
    toolVersion = detektVersion
    config = files("$projectDir/detekt.yml")
    idea {
        path = ".idea"
        codeStyleScheme = ".idea/code-style.xml"
        inspectionsProfile = ".idea/inspect.xml"
        report = "$projectDir/reports"
        mask = "*.kt,"
    }
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt> {
    // include("**/special/package/**") // only analyze a sub package inside src/main/kotlin
    exclude(".*test.*,.*/resources/.*,.*/tmp/.*")
}

val lint by tasks.creating(DefaultTask::class) {
    description = "Runs detekt and validateTaskProperties"
    group = "Verification"
    // Does this task come from java-gradle-plugin?
    dependsOn("validatePlugins")
    dependsOn("detekt")
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
    dependsOn("detekt")
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
        repo = property("bintray.repo") as String
        name = project.name
        desc = project.description
        websiteUrl = projectUrl
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
            vcsTag = project.version.toString()
            attributes = mapOf("gradle-plugin" to "${project.group}:$artifactName:${project.version}")

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
