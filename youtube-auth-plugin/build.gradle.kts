plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.plugin.publish)
    id("education.cccp.build.gradle-plugin") version "0.0.7"
    id("education.cccp.build.publishing") version "0.0.7"
    id("education.cccp.build.functional-test") version "0.0.7"
}

group = "education.cccp"
// HF-YOUTUBE-AUTH — first release.
version = "0.0.2"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    // Cross-borough versions (kotlin/cucumber/junit) ride the workspace BOM.
    implementation(platform("education.cccp:workspace-bom:0.0.99"))
    implementation(kotlin("stdlib-jdk8"))

    testImplementation(kotlin("test-junit5"))
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.assertj)

    // Cucumber BDD — acceptance for the pure OAuth domain.
    testImplementation(libs.cucumber.java)
    testImplementation(libs.cucumber.junit.platform.engine)
    testImplementation(libs.cucumber.picocontainer)
    testImplementation(libs.junit.platform.suite)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Cucumber BDD — features in src/test/resources/features, steps in src/test/kotlin/video/auth/scenarios.
sourceSets.test {
    resources.srcDir("src/test/resources/features")
}

val cucumberTest = tasks.register<Test>("cucumberTest") {
    description = "Runs Cucumber BDD tests"
    group = "verification"
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = configurations.testRuntimeClasspath.get() +
        sourceSets.test.get().output +
        sourceSets.main.get().output +
        files(tasks.named("jar").get().outputs.files)

    dependsOn(tasks.classes)
    useJUnitPlatform {
        excludeEngines("junit-jupiter")
    }
    filter {
        includeTestsMatching("video.auth.scenarios.CucumberRunner")
    }
    systemProperty("cucumber.junit-platform.naming-strategy", "long")
    val cucumberTags = project.findProperty("cucumber.tags")?.toString() ?: "not @wip"
    systemProperty("cucumber.filter.tags", cucumberTags)
}

tasks.named("check") { dependsOn(cucumberTest) }

// Expose the plugin artefact version in the jar manifest (diagnostics / N3).
tasks.named<Jar>("jar") {
    manifest {
        attributes["Implementation-Version"] = project.version
    }
}

gradlePlugin {
    plugins {
        create("youtubeAuth") {
            id = "education.cccp.video.auth"
            implementationClass = "video.auth.YouTubeAuthPlugin"
            displayName = "YouTube Auth Plugin"
            description = "Plugin Gradle N1 fournissant la tache obtainYouTubeRefreshToken " +
                "(flux OAuth 2.0 loopback) pour obtenir un refresh token YouTube, reutilisable " +
                "par les boroughs consommateurs (video-kernel)."
            tags.set(listOf("youtube", "oauth", "video", "gradle"))
        }
    }
    website = "https://github.com/cccp-education/video-kernel-gradle"
    vcsUrl = "https://github.com/cccp-education/video-kernel-gradle.git"
}

publishingConventions {
    publicationType = "PLUGIN"
}

publishing {
    publications.withType<MavenPublication> {
        pom {
            name.set("YouTube Auth Gradle Plugin")
            description.set(
                "Plugin Gradle N1 fournissant la tache obtainYouTubeRefreshToken " +
                    "(flux OAuth 2.0 loopback) : obtention d'un refresh token YouTube, consomme " +
                    "par les boroughs video (capsule, hyperframes) via le port video.kernel.publish.",
            )
        }
    }
    repositories {
        mavenCentral()
    }
}
