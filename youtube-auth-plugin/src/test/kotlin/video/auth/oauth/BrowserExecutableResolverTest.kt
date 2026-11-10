package video.auth.oauth

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * TDD — pure [BrowserExecutableResolver] (HF-YOUTUBE-AUTH US-4).
 *
 * The existence probe is injected, so the precedence is verified without depending on
 * which browser happens to be installed on the build host.
 */
class BrowserExecutableResolverTest {
    private val candidates = listOf("google-chrome", "chromium")

    @Test
    fun `explicit executable wins over candidates`() {
        val resolved = BrowserExecutableResolver.resolve("brave-browser", candidates) { true }

        assertThat(resolved).isEqualTo("brave-browser")
    }

    @Test
    fun `first executable candidate is selected`() {
        val resolved =
            BrowserExecutableResolver.resolve(null, candidates) { it == "chromium" }

        assertThat(resolved).isEqualTo("chromium")
    }

    @Test
    fun `blank configured value falls back to candidates`() {
        val resolved = BrowserExecutableResolver.resolve("  ", candidates) { it == "google-chrome" }

        assertThat(resolved).isEqualTo("google-chrome")
    }

    @Test
    fun `no executable yields null`() {
        val resolved = BrowserExecutableResolver.resolve(null, candidates) { false }

        assertThat(resolved).isNull()
    }

    @Test
    fun `a configured non executable is skipped`() {
        val resolved =
            BrowserExecutableResolver.resolve("/missing/chrome", candidates) { it == "chromium" }

        assertThat(resolved).isEqualTo("chromium")
    }

    @Test
    fun `linux candidates include chrome and chromium`() {
        assertThat(BrowserExecutableResolver.candidates("Linux"))
            .contains("google-chrome", "chromium")
    }

    @Test
    fun `mac candidates target application bundles`() {
        assertThat(BrowserExecutableResolver.candidates("Mac OS X"))
            .contains("/Applications/Google Chrome.app/Contents/MacOS/Google Chrome")
    }

    @Test
    fun `windows candidates include chrome executable`() {
        assertThat(BrowserExecutableResolver.candidates("Windows 11"))
            .contains("chrome.exe")
    }
}
