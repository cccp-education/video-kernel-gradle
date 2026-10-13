plugins {
    `java-library`
    `maven-publish`
    alias(libs.plugins.kotlin.jvm)
    id("education.cccp.build.publishing") version "0.0.7"
}

group = "education.cccp"
version = "0.0.3"
kotlin.jvmToolchain(JavaVersion.VERSION_25.ordinal)

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    // Neutral ports/model expose the pure N0 i18n contract (SupportedLanguage).
    api(libs.i18n.contracts)
    implementation(kotlin("stdlib-jdk8"))

    testImplementation(kotlin("test-junit5"))
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.assertj)
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
    }
}

publishingConventions {
    publicationType = "LIBRARY"
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            pom {
                name.set("VideoKernel")
                description.set(
                    "N1 shared library — engine-neutral video model and ports " +
                        "(segments, visual sources, production plan, video engine, segment source)."
                )
            }
        }
    }
    repositories {
        mavenCentral()
    }
}
