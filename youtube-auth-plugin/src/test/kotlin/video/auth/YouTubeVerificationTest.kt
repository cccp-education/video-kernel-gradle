package video.auth

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * TDD — pure verification domain of the YouTube preflight (HF-YOUTUBE-AUTH US-5).
 */
class YouTubeVerificationTest {
    private val channel = YouTubeChannel(id = "UC6KzSrC4wCf-066VwEU5Vww", title = "cccp.education")

    @Test
    fun `the refresh grant carries the four oauth keys`() {
        val form = YouTubeRefreshRequest.form("cid", "GOCSPX-x", "1//0gXYZ")

        assertThat(form)
            .containsEntry("client_id", "cid")
            .containsEntry("client_secret", "GOCSPX-x")
            .containsEntry("refresh_token", "1//0gXYZ")
            .containsEntry("grant_type", "refresh_token")
        assertThat(form).hasSize(4)
    }

    @Test
    fun `an unconfigured expectation never mismatches`() {
        val check = YouTubeChannelCheck(expectedChannelId = "", actual = channel)

        assertThat(check.isConfigured()).isFalse()
        assertThat(check.matches()).isTrue()
        assertThat(check.summary()).contains("UC6KzSrC4wCf-066VwEU5Vww").contains("cccp.education")
    }

    @Test
    fun `a matching configured channel is verified`() {
        val check = YouTubeChannelCheck(expectedChannelId = "UC6KzSrC4wCf-066VwEU5Vww", actual = channel)

        assertThat(check.isConfigured()).isTrue()
        assertThat(check.matches()).isTrue()
        assertThat(check.summary()).contains("matches")
    }

    @Test
    fun `a mismatched configured channel is reported with both ids`() {
        val check = YouTubeChannelCheck(expectedChannelId = "UCexpectedChannel0000000000", actual = channel)

        assertThat(check.matches()).isFalse()
        assertThat(check.summary())
            .contains("mismatch")
            .contains("UCexpectedChannel0000000000")
            .contains("UC6KzSrC4wCf-066VwEU5Vww")
    }
}
