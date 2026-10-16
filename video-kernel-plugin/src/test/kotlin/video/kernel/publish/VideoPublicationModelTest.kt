package video.kernel.publish

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

/**
 * TDD — invariants of the publication value objects (HF-YOUTUBE-PUBLISH US-1).
 */
class VideoPublicationModelTest {

    @Test
    fun `a chapter rejects a negative start`() {
        assertThatThrownBy { VideoChapter(-1, "Intro") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("startSeconds")
    }

    @Test
    fun `a chapter rejects a blank title`() {
        assertThatThrownBy { VideoChapter(0, " ") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("title")
    }

    @Test
    fun `a link rejects a blank label`() {
        assertThatThrownBy { VideoLink(" ", "https://cccp.education/") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("label")
    }

    @Test
    fun `a link rejects a blank url`() {
        assertThatThrownBy { VideoLink("Site", "") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("url")
    }

    @Test
    fun `empty links report themselves empty`() {
        assertThat(VideoLinks().isEmpty()).isTrue()
        assertThat(VideoLinks(nextVideo = "https://youtu.be/x").isEmpty()).isFalse()
        assertThat(VideoLinks(entries = listOf(VideoLink("S", "u"))).isEmpty()).isFalse()
    }

    @Test
    fun `publication defaults are neutral and private`() {
        val publication = VideoPublication(videoFile = java.io.File("v.mp4"), title = "T")

        assertThat(publication.visibility).isEqualTo(VideoVisibility.PRIVATE)
        assertThat(publication.tags).isEmpty()
        assertThat(publication.hashtags).isEmpty()
        assertThat(publication.chapters).isEmpty()
        assertThat(publication.thumbnailFile).isNull()
        assertThat(publication.publishAt).isNull()
        assertThat(publication.madeForKids).isFalse()
    }

    @Test
    fun `the publisher port is a SAM lambda`() {
        val publisher = VideoPublisher { VideoPublicationResult.Published("id", "https://youtu.be/id") }

        val result = publisher.publish(VideoPublication(java.io.File("v.mp4"), "T"))

        assertThat(result).isEqualTo(VideoPublicationResult.Published("id", "https://youtu.be/id"))
    }
}
