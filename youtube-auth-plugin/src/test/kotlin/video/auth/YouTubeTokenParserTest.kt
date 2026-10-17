package video.auth

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

/**
 * TDD — pure [YouTubeTokenParser] (HF-YOUTUBE-AUTH US-1).
 */
class YouTubeTokenParserTest {

    @Test
    fun `parses refresh and access tokens`() {
        val response = YouTubeTokenParser.parse(
            """{"access_token":"ya29.abc","refresh_token":"1//0gXYZ","expires_in":3599,"token_type":"Bearer"}""",
        )

        assertThat(response.accessToken).isEqualTo("ya29.abc")
        assertThat(response.refreshToken).isEqualTo("1//0gXYZ")
        assertThat(response.hasRefreshToken()).isTrue()
    }

    @Test
    fun `a response without a refresh token is reported as such`() {
        val response = YouTubeTokenParser.parse("""{"access_token":"ya29.abc","expires_in":3599}""")

        assertThat(response.refreshToken).isNull()
        assertThat(response.hasRefreshToken()).isFalse()
    }

    @Test
    fun `parses an error response`() {
        val response = YouTubeTokenParser.parse(
            """{"error":"invalid_grant","error_description":"Bad Request"}""",
        )

        assertThat(response.error).isEqualTo("invalid_grant")
        assertThat(response.errorDescription).isEqualTo("Bad Request")
        assertThat(response.hasRefreshToken()).isFalse()
    }

    @Test
    fun `tolerates surrounding whitespace in the json`() {
        val response = YouTubeTokenParser.parse(
            "  { \"refresh_token\" : \"1//0g\" }  ",
        )

        assertThat(response.refreshToken).isEqualTo("1//0g")
    }

    @Test
    fun `blank json is rejected`() {
        assertThatThrownBy { YouTubeTokenParser.parse("  ") }
            .isInstanceOf(IllegalArgumentException::class.java)
    }
}
