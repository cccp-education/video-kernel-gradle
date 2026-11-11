package video.auth

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * TDD — pure [YouTubeChannelProbe] (HF-YOUTUBE-AUTH US-5).
 */
class YouTubeChannelProbeTest {
    private val channelsJson =
        """
        {
          "kind": "youtube#channelListResponse",
          "items": [
            {
              "kind": "youtube#channel",
              "id": "UC6KzSrC4wCf-066VwEU5Vww",
              "snippet": { "title": "cccp.education", "customUrl": "@cccp.education" }
            }
          ]
        }
        """.trimIndent()

    @Test
    fun `extracts the channel id and title`() {
        val channel = YouTubeChannelProbe.parse(channelsJson)

        assertThat(channel).isNotNull()
        assertThat(channel!!.id).isEqualTo("UC6KzSrC4wCf-066VwEU5Vww")
        assertThat(channel.title).isEqualTo("cccp.education")
    }

    @Test
    fun `a blank body yields no channel`() {
        assertThat(YouTubeChannelProbe.parse("")).isNull()
        assertThat(YouTubeChannelProbe.parse("   ")).isNull()
    }

    @Test
    fun `a response without a UC id yields no channel`() {
        assertThat(YouTubeChannelProbe.parse("""{"items":[]}""")).isNull()
    }

    @Test
    fun `a video id is never mistaken for a channel id`() {
        assertThat(YouTubeChannelProbe.parse("""{"items":[{"id":"dQw4w9WgXcQ"}]}""")).isNull()
    }

    @Test
    fun `a missing title is tolerated`() {
        val channel = YouTubeChannelProbe.parse("""{"items":[{"id":"UCabcdefghijklmnopqrstuv"}]}""")

        assertThat(channel).isNotNull()
        assertThat(channel!!.title).isEmpty()
    }
}
