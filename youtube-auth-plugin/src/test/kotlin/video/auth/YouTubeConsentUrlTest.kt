package video.auth

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/**
 * TDD — pure [YouTubeConsentUrl] (HF-YOUTUBE-AUTH US-1).
 */
class YouTubeConsentUrlTest {

    @Test
    fun `loopback redirect is a localhost url on the given port`() {
        assertThat(YouTubeConsentUrl.loopbackRedirect(8765)).isEqualTo("http://localhost:8765/")
    }

    @Test
    fun `build targets the google auth endpoint with oauth code flow`() {
        val url = YouTubeConsentUrl.build("cid.apps.googleusercontent.com", YouTubeConsentUrl.loopbackRedirect(8765))

        val query = decodeQuery(url.substringAfter("?"))
        assertThat(url).startsWith(YouTubeConsentUrl.AUTH_ENDPOINT)
        assertThat(query["client_id"]).isEqualTo("cid.apps.googleusercontent.com")
        assertThat(query["redirect_uri"]).isEqualTo("http://localhost:8765/")
        assertThat(query["response_type"]).isEqualTo("code")
        assertThat(query["scope"]).isEqualTo(YouTubeConsentUrl.YOUTUBE_UPLOAD_SCOPE)
    }

    @Test
    fun `build requests offline access with a fresh consent`() {
        val query = decodeQuery(
            YouTubeConsentUrl.build("cid", "http://localhost:8765/").substringAfter("?"),
        )

        assertThat(query["access_type"]).isEqualTo("offline")
        assertThat(query["prompt"]).isEqualTo("consent")
    }

    @Test
    fun `build honours a custom scope`() {
        val query = decodeQuery(
            YouTubeConsentUrl.build("cid", "http://localhost:1/", "scope-x").substringAfter("?"),
        )

        assertThat(query["scope"]).isEqualTo("scope-x")
    }

    @Test
    fun `blank client id is rejected`() {
        assertThatThrownBy { YouTubeConsentUrl.build(" ", "http://localhost:1/") }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    private fun decodeQuery(query: String): Map<String, String> =
        query.split("&").associate {
            val (key, value) = it.split("=", limit = 2)
            URLDecoder.decode(key, StandardCharsets.UTF_8) to URLDecoder.decode(value, StandardCharsets.UTF_8)
        }
}
