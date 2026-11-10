package video.auth.oauth

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

/**
 * TDD — [BrowserLauncher] with injected process/desktop seams (HF-YOUTUBE-AUTH US-4).
 *
 * The launch itself is N2 I/O; the seams keep the isolation verdict and the built
 * command fully testable without opening a real browser.
 */
class BrowserLauncherTest {
    @TempDir
    lateinit var tempDir: Path

    private val consentUrl = "https://accounts.google.com/o/oauth2/v2/auth?client_id=cid"

    @Test
    fun `opens a fresh isolated profile on every run`() {
        val commands = mutableListOf<List<String>>()

        val result =
            launch(
                options = BrowserLaunchOptions(executable = "fake-chrome", profileDir = tempDir.toFile()),
                commands = commands,
            )

        assertThat(result).isInstanceOf(BrowserLaunchResult.OpenedInIsolatedProfile::class.java)
        val opened = result as BrowserLaunchResult.OpenedInIsolatedProfile
        assertThat(opened.ephemeral).isTrue()
        assertThat(opened.profileDir).exists()
        assertThat(opened.profileDir.parentFile).isEqualTo(tempDir.toFile())
        assertThat(commands.single()).contains("--user-data-dir=${opened.profileDir.absolutePath}")
        assertThat(commands.single().last()).isEqualTo(consentUrl)
    }

    @Test
    fun `two fresh launches never share a profile directory`() {
        val first = freshProfileDir()
        val second = freshProfileDir()

        assertThat(first).isNotEqualTo(second)
    }

    @Test
    fun `reuses the isolated profile when freshProfile is false`() {
        val result =
            launch(
                options =
                    BrowserLaunchOptions(
                        executable = "fake-chrome",
                        profileDir = tempDir.toFile(),
                        freshProfile = false,
                    ),
            )

        val opened = result as BrowserLaunchResult.OpenedInIsolatedProfile
        assertThat(opened.ephemeral).isFalse()
        assertThat(opened.profileDir).isEqualTo(tempDir.toFile())
    }

    @Test
    fun `falls back to the default browser when no chromium is available`() {
        var desktopOpened = false

        val result =
            BrowserLauncher.open(
                url = consentUrl,
                options = BrowserLaunchOptions(executable = "missing-chrome", profileDir = tempDir.toFile()),
                candidates = emptyList(),
                probe = { false },
                launcher = { error("must not launch") },
                desktopOpener = {
                    desktopOpened = true
                    true
                },
            )

        assertThat(result).isInstanceOf(BrowserLaunchResult.OpenedInDefaultBrowser::class.java)
        assertThat(desktopOpened).isTrue()
    }

    @Test
    fun `reports unavailable when neither chromium nor desktop is available`() {
        val result =
            BrowserLauncher.open(
                url = consentUrl,
                options = BrowserLaunchOptions(profileDir = tempDir.toFile()),
                candidates = emptyList(),
                probe = { false },
                launcher = { error("must not launch") },
                desktopOpener = { false },
            )

        assertThat(result).isEqualTo(BrowserLaunchResult.Unavailable)
    }

    @Test
    fun `falls back to the default browser when chromium fails to start`() {
        var desktopOpened = false

        val result =
            BrowserLauncher.open(
                url = consentUrl,
                options = BrowserLaunchOptions(executable = "fake-chrome", profileDir = tempDir.toFile()),
                candidates = emptyList(),
                probe = { true },
                launcher = { throw IllegalStateException("cannot start") },
                desktopOpener = {
                    desktopOpened = true
                    true
                },
            )

        assertThat(result).isInstanceOf(BrowserLaunchResult.OpenedInDefaultBrowser::class.java)
        assertThat(desktopOpened).isTrue()
    }

    private fun launch(
        options: BrowserLaunchOptions,
        commands: MutableList<List<String>> = mutableListOf(),
    ): BrowserLaunchResult =
        BrowserLauncher.open(
            url = consentUrl,
            options = options,
            candidates = emptyList(),
            probe = { it == options.executable },
            launcher = { commands += it },
            desktopOpener = { false },
        )

    private fun freshProfileDir(): File {
        val result =
            launch(
                options =
                    BrowserLaunchOptions(
                        executable = "fake-chrome",
                        profileDir = tempDir.toFile(),
                    ),
            )
        return (result as BrowserLaunchResult.OpenedInIsolatedProfile).profileDir
    }
}
