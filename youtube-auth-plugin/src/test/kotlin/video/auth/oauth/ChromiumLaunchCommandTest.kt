package video.auth.oauth

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.io.File

/**
 * TDD — pure [ChromiumLaunchCommand] (HF-YOUTUBE-AUTH US-4).
 *
 * The command line is the contract that isolates the OAuth consent session: a
 * dedicated `--user-data-dir` means Chromium never reads the cookies of the default
 * profile, so Google cannot reuse an already signed-in (wrong) account.
 */
class ChromiumLaunchCommandTest {
    private val profileDir = File("/tmp/youtube-auth-profile/session-1")

    @Test
    fun `command uses the dedicated user data dir`() {
        val command = ChromiumLaunchCommand.build("google-chrome", profileDir, "https://accounts.google.com/x")

        assertThat(command).contains("--user-data-dir=${profileDir.absolutePath}")
    }

    @Test
    fun `command disables first run and default browser checks`() {
        val command = ChromiumLaunchCommand.build("google-chrome", profileDir, "https://accounts.google.com/x")

        assertThat(command).contains("--no-first-run", "--no-default-browser-check")
    }

    @Test
    fun `command ends with the consent url`() {
        val command = ChromiumLaunchCommand.build("google-chrome", profileDir, "https://accounts.google.com/x")

        assertThat(command.first()).isEqualTo("google-chrome")
        assertThat(command.last()).isEqualTo("https://accounts.google.com/x")
    }

    @Test
    fun `extra args are forwarded before the url`() {
        val command =
            ChromiumLaunchCommand.build(
                executable = "google-chrome",
                profileDir = profileDir,
                url = "https://accounts.google.com/x",
                extraArgs = listOf("--lang=en"),
            )

        assertThat(command).containsSubsequence("--lang=en", "https://accounts.google.com/x")
    }

    @Test
    fun `blank executable is rejected`() {
        assertThatThrownBy {
            ChromiumLaunchCommand.build(" ", profileDir, "https://accounts.google.com/x")
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `blank url is rejected`() {
        assertThatThrownBy {
            ChromiumLaunchCommand.build("google-chrome", profileDir, " ")
        }.isInstanceOf(IllegalArgumentException::class.java)
    }
}
