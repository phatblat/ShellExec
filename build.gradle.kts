/*
 * build.gradle.kts
 * ShellExec
 */

/* -------------------------------------------------------------------------- */
// 🛃 Imports
/* -------------------------------------------------------------------------- */

@file:Suppress("SpellCheckingInspection")

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import org.gradle.api.file.DuplicatesStrategy.INCLUDE
import org.gradle.api.tasks.wrapper.Wrapper.DistributionType.BIN
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/* -------------------------------------------------------------------------- */
// 🔌 Plugins
/* -------------------------------------------------------------------------- */

plugins {
    // Gradle built-in
    jacoco
    `java-gradle-plugin`
    `maven-publish`

    alias(libs.plugins.detekt)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.publish)
}

/* -------------------------------------------------------------------------- */
// 📋 Properties
/* -------------------------------------------------------------------------- */

val artifactId: String by project
val javaPackage = "$group.$artifactId"
val pluginClass: String by project
val projectUrl: String by project
val tags: String by project
val labels = tags.split(",")
val license: String by project

val jvmTarget = JavaVersion.VERSION_17

tasks.wrapper {
    gradleVersion = libs.versions.gradle.get()
    distributionType = BIN
}

/* -------------------------------------------------------------------------- */
// 👪 Dependencies
/* -------------------------------------------------------------------------- */

java.toolchain.languageVersion = JavaLanguageVersion.of(17)
val javaLauncher = javaToolchains.launcherFor {
    languageVersion = JavaLanguageVersion.of(17)
}
//java.toolchain.languageVersion.get()

repositories.gradlePluginPortal()

dependencies {
    implementation(libs.commons.exec)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.stdlib.jdk8)

    testImplementation(libs.kotlin.test.asProvider().get())
    testImplementation(libs.kotlin.test.junit5)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.spek2.dsl)

    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.spek2.runner.junit5)
}

/* -------------------------------------------------------------------------- */
// 🏗 Assemble
/* -------------------------------------------------------------------------- */

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = jvmTarget.toString()
}

java {
    sourceCompatibility = jvmTarget
    targetCompatibility = jvmTarget
}

// Include resources
sourceSets["main"].resources {
    srcDirs("src/main/resources")
    include("VERSION.txt")
}

val updateVersionFile: Task by tasks.creating {
    description = "Updates the VERSION.txt file included with the plugin"
    group = "Build"
    doLast {
        val resources = layout.projectDirectory.dir("src/main/resources")
        project.file(resources).mkdirs()

        val versionFile = resources.file("VERSION.txt").asFile
        versionFile.createNewFile()
        versionFile.writeText(version.toString())
    }
}

afterEvaluate {
    tasks.named("processResources").configure {
        dependsOn(updateVersionFile)
        val task = this as AbstractCopyTask
        // Workaround for error 👇🏻
        // Execution failed for task ":processResources".
        //> Entry VERSION.txt is a duplicate but no duplicate handling strategy has been set. Please refer to https://docs.gradle.org/7.4.2/dsl/org.gradle.api.tasks.Copy.html#org.gradle.api.tasks.Copy:duplicatesStrategy for details.
        task.duplicatesStrategy = INCLUDE
    }
}

val sourcesJar by tasks.creating(Jar::class) {
    dependsOn("classes")
    from(sourceSets["main"].allJava)
    exclude("src/main/resources/VERSION.txt")
}

val javadocJar by tasks.creating(Jar::class) {
    dependsOn("javadoc")
    val javadoc = tasks.withType<Javadoc>().first()
    from(javadoc.destinationDir)
}

artifacts.add("archives", sourcesJar)
artifacts.add("archives", javadocJar)

base {
    // at.phatbl.shellexec-1.0.0.jar
    archivesName = artifactId
}

// https://plugins.gradle.org/docs/publish-plugin#examples
gradlePlugin {
    @Suppress("UnstableApiUsage")
    website = projectUrl
    @Suppress("UnstableApiUsage")
    vcsUrl = projectUrl
    description = project.description

    plugins {
        create(artifactId) {
            id = javaPackage
            implementationClass = "$javaPackage.$pluginClass"
            displayName = project.name
            description = project.description
            @Suppress("UnstableApiUsage")
            tags.set(labels)
            version = project.version.toString()
        }
    }
}

/* -------------------------------------------------------------------------- */
// ✅ Test
/* -------------------------------------------------------------------------- */

tasks.named<Test>("test") {
    useJUnitPlatform {
        includeEngines("spek2", "junit-jupiter")
    }
}

// https://docs.gradle.org/current/userguide/jacoco_plugin.html#sec:jacoco_getting_started
jacoco {
    toolVersion = libs.versions.jacoco.get()
    reportsDirectory.set(layout.buildDirectory.dir("reports/jacoco"))
}

tasks.jacocoTestReport {
    reports {
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacocoHtml"))
        xml.required.set(true)
        xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco.xml"))
        csv.required.set(false)
    }
}

val codeCoverageReport by tasks.creating(JacocoReport::class) {
    dependsOn("test")
}

/* -------------------------------------------------------------------------- */
// 🔍 Code Quality
/* -------------------------------------------------------------------------- */

// https://detekt.dev/
detekt {
    toolVersion = libs.versions.detekt.get()
    config.setFrom("$projectDir/detekt.yml")
}

javaLauncher.map {
    tasks.withType<Detekt>().configureEach {
        jvmTarget = java.toolchain.languageVersion.get().toString()
        jdkHome.set(file(it.executablePath))
        exclude(".*test.*,.*/resources/.*,.*/tmp/.*")
    }
    tasks.withType<DetektCreateBaselineTask>().configureEach {
        jvmTarget = java.toolchain.languageVersion.get().toString()
        jdkHome.set(file(it.executablePath))
    }
}

val lint by tasks.creating(DefaultTask::class) {
    description = "Runs detekt and validateTaskProperties"
    group = "Verification"
    // Does this task come from java-gradle-plugin?
    dependsOn("validatePlugins")
    dependsOn("detekt")
}

val codeQuality by tasks.creating(DefaultTask::class) {
    description = "Runs all code quality checks."
    dependsOn("detekt")
    dependsOn("check")
    dependsOn(lint)
}

/* -------------------------------------------------------------------------- */
// Release
/* -------------------------------------------------------------------------- */

val release by tasks.creating(DefaultTask::class) {
    description = "Performs release actions."
    doLast { logger.lifecycle("Release task not implemented.") }
}

/* -------------------------------------------------------------------------- */
// 🚀 Deployment
/* -------------------------------------------------------------------------- */

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
            artifactId = artifactId

            artifact(sourcesJar) { classifier = "sources" }
            artifact(javadocJar) { classifier = "javadoc" }
        }
    }
}

val deploy: Task by tasks.creating {
    description = "Deploys plugin to the Gradle plugin portal."
    dependsOn("publishPlugins")
}
