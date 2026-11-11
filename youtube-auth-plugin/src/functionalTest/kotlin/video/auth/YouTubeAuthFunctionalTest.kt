package video.auth

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import kotlin.test.assertContains

/**
 * Functional acceptance (Gradle TestKit) for the `obtainYouTubeRefreshToken`
 * task (HF-YOUTUBE-AUTH US-2).
 *
 * Hermetic: it exercises task registration and the pre-flight validation paths,
 * never the interactive browser flow.
 */
class YouTubeAuthFunctionalTest {
    @TempDir
    lateinit var tempDir: Path
    private lateinit var projectDir: File

    @BeforeEach
    fun setUp() {
        projectDir = tempDir.toFile()
        writeBuildFiles()
    }

    @Test
    fun `plugin registers the obtainYouTubeRefreshToken task under deploy`() {
        val result = runner("tasks", "--group=deploy")

        assertContains(result.output, "obtainYouTubeRefreshToken")
    }

    @Test
    fun `the task fails clearly when the credentials file is missing`() {
        val result =
            GradleRunner
                .create()
                .withProjectDir(projectDir)
                .withPluginClasspath()
                .withArguments("obtainYouTubeRefreshToken")
                .buildAndFail()

        assertContains(result.output, "credentials file not found")
    }

    @Test
    fun `the task fails clearly when clientId is missing`() {
        projectDir.resolve("youtube.yaml").writeText("youtube:\n  clientSecret: \"GOCSPX-x\"\n")

        val result =
            GradleRunner
                .create()
                .withProjectDir(projectDir)
                .withPluginClasspath()
                .withArguments("obtainYouTubeRefreshToken")
                .buildAndFail()

        assertContains(result.output, "clientId is missing")
    }

    @Test
    fun `plugin registers the verifyYouTubeCredentials task under deploy`() {
        val result = runner("tasks", "--group=deploy")

        assertContains(result.output, "verifyYouTubeCredentials")
    }

    @Test
    fun `the preflight fails clearly when the credentials file is missing`() {
        val result =
            GradleRunner
                .create()
                .withProjectDir(projectDir)
                .withPluginClasspath()
                .withArguments("verifyYouTubeCredentials")
                .buildAndFail()

        assertContains(result.output, "credentials file not found")
    }

    @Test
    fun `the preflight fails clearly when the refresh token is missing`() {
        projectDir
            .resolve("youtube.yaml")
            .writeText("youtube:\n  clientId: \"cid\"\n  clientSecret: \"GOCSPX-x\"\n")

        val result =
            GradleRunner
                .create()
                .withProjectDir(projectDir)
                .withPluginClasspath()
                .withArguments("verifyYouTubeCredentials")
                .buildAndFail()

        assertContains(result.output, "missing refreshToken")
    }

    private fun runner(vararg args: String) =
        GradleRunner
            .create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments(*args)
            .build()

    private fun writeBuildFiles() {
        projectDir.resolve("settings.gradle.kts").writeText(
            """
            pluginManagement {
                repositories {
                    mavenLocal()
                    gradlePluginPortal()
                    mavenCentral()
                }
            }
            rootProject.name = "test-youtube-auth"
            """.trimIndent(),
        )
        projectDir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                id("education.cccp.video.auth")
            }
            youtubeAuth {
                credentialsFile.set("youtube.yaml")
                port.set(8765)
            }
            """.trimIndent(),
        )
    }
}
