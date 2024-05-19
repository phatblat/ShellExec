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
import org.gradle.api.file.DuplicatesStrategy
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

    id("io.gitlab.arturbosch.detekt") version "1.23.6"

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

val jvmTarget = JavaVersion.VERSION_1_8

val commonsExecVersion: String by project
val junitVersion: String by project
val spekVersion: String by project
val detektVersion: String by project
val jacocoVersion: String by project
val gradleWrapperVersion: String by project

tasks.wrapper {
    gradleVersion = gradleWrapperVersion
    distributionType = BIN
}

/* -------------------------------------------------------------------------- */
// 👪 Dependencies
/* -------------------------------------------------------------------------- */

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}
val javaLauncher = javaToolchains.launcherFor {
    languageVersion = JavaLanguageVersion.of(17)
}
//java.toolchain.languageVersion.get()

repositories.gradlePluginPortal()

dependencies {
    api("org.apache.commons:commons-exec:$commonsExecVersion")
    implementation(kotlin("stdlib"))
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
    testImplementation(platform("org.junit:junit-bom:$junitVersion"))
    testImplementation("org.junit.platform:junit-platform-runner")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.jetbrains.spek:spek-api:$spekVersion")
    testRuntimeOnly("org.jetbrains.spek:spek-junit-platform-engine:$spekVersion")
}

/* -------------------------------------------------------------------------- */
// 🏗 Assemble
/* -------------------------------------------------------------------------- */

tasks.withType<KotlinCompile> { kotlinOptions.jvmTarget = "$jvmTarget" }

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
        // Execution failed for task ':processResources'.
        //> Entry VERSION.txt is a duplicate but no duplicate handling strategy has been set. Please refer to https://docs.gradle.org/7.4.2/dsl/org.gradle.api.tasks.Copy.html#org.gradle.api.tasks.Copy:duplicatesStrategy for details.
        task.duplicatesStrategy = DuplicatesStrategy.INCLUDE
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

gradlePlugin.plugins.create(artifactId) {
    id = javaPackage
    implementationClass = "$javaPackage.$pluginClass"
}

pluginBundle {
    website = projectUrl
    vcsUrl = projectUrl
    description = project.description

    (plugins) {
        artifactId {
            id = javaPackage
            displayName = project.name
            tags = labels
            version = project.version.toString()
        }
    }
    mavenCoordinates.artifactId = artifactId
}

/* -------------------------------------------------------------------------- */
// ✅ Test
/* -------------------------------------------------------------------------- */

tasks.test {
    jvmArgs(
        "--add-opens", "java.base/java.lang=ALL-UNNAMED",
        "--add-opens", "java.base/java.util=ALL-UNNAMED"
    )
}

// https://docs.gradle.org/current/userguide/jacoco_plugin.html#sec:jacoco_getting_started
jacoco {
    toolVersion = jacocoVersion
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
    // sourceSets(sourceSets["main"])
}

/* -------------------------------------------------------------------------- */
// 🔍 Code Quality
/* -------------------------------------------------------------------------- */

// https://arturbosch.github.io/detekt/kotlindsl.html
// https://detekt.dev/
detekt {
    toolVersion = detektVersion
    config.setFrom("$projectDir/detekt.yml")
}

javaLauncher.map { it ->
    tasks.withType<Detekt>().configureEach {
        jvmTarget = "1.8"
        jdkHome.set(file(it.executablePath))

        // include("**/special/package/**") // only analyze a sub package inside src/main/kotlin
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
            artifactId = artifactId

            artifact(sourcesJar) { classifier = "sources" }
            artifact(javadocJar) { classifier = "javadoc" }
        }
    }
}

val deploy: Task by tasks.creating {
    description = "Deploys plugin to the Gradle plugin portal."
    group = "🚇 Tube"
    dependsOn("publishPlugins")
}
