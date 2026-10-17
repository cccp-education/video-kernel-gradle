package video.auth

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

/**
 * TDD — pure [CredentialsFileRewriter] (HF-YOUTUBE-AUTH US-1).
 */
class CredentialsFileRewriterTest {

    private val yaml = """
        youtube:
          clientId: "cid.apps.googleusercontent.com"
          clientSecret: "GOCSPX-xyz"
          refreshToken: ""
          channelId: "UC123"
          # Optionnel — lecture publique uniquement
          apiKey: ""
    """.trimIndent() + "\n"

    @Test
    fun `reads the client id and secret`() {
        assertThat(CredentialsFileRewriter.clientId(yaml)).isEqualTo("cid.apps.googleusercontent.com")
        assertThat(CredentialsFileRewriter.clientSecret(yaml)).isEqualTo("GOCSPX-xyz")
    }

    @Test
    fun `an empty refresh token reads as null`() {
        assertThat(CredentialsFileRewriter.refreshToken(yaml)).isNull()
    }

    @Test
    fun `writes the refresh token in place, preserving every other line`() {
        val updated = CredentialsFileRewriter.withRefreshToken(yaml, "1//0gNEW")

        assertThat(updated).contains("""refreshToken: "1//0gNEW"""")
        assertThat(updated).contains("""clientSecret: "GOCSPX-xyz"""")
        assertThat(updated).contains("""channelId: "UC123"""")
        assertThat(updated).contains("# Optionnel")
        // Only the refresh token line changed.
        assertThat(updated.lines().count()).isEqualTo(yaml.lines().count())
    }

    @Test
    fun `preserves the trailing newline`() {
        assertThat(CredentialsFileRewriter.withRefreshToken(yaml, "1//x")).endsWith("\n")
        assertThat(CredentialsFileRewriter.withRefreshToken(yaml.trimEnd(), "1//x")).doesNotEndWith("\n")
    }

    @Test
    fun `adds the field when the file has none`() {
        val minimal = "youtube:\n  clientId: \"cid\"\n"

        val updated = CredentialsFileRewriter.withRefreshToken(minimal, "1//0g")

        assertThat(updated).contains("""refreshToken: "1//0g"""")
        assertThat(updated).contains("""clientId: "cid"""")
    }

    @Test
    fun `a blank token is rejected`() {
        assertThatThrownBy { CredentialsFileRewriter.withRefreshToken(yaml, "  ") }
            .isInstanceOf(IllegalArgumentException::class.java)
    }
}
