package video.kernel.publish

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * TDD — pure [VideoPublicationValidator] (HF-YOUTUBE-PUBLISH US-1).
 *
 * Every rule is checked without any I/O beyond a real temporary video file
 * (existence is a genuine rule).
 */
class VideoPublicationValidatorTest {

    @TempDir
    lateinit var tempDir: File

    private fun video(): File = File(tempDir, "video.mp4").apply { writeText("x") }

    private fun valid(tags: List<String> = emptyList(), chapters: List<VideoChapter> = emptyList()) =
        VideoPublication(
            videoFile = video(),
            title = "A title",
            tags = tags,
            chapters = chapters,
        )

    @Test
    fun `a minimal publication is valid`() {
        assertThat(VideoPublicationValidator.validate(valid()).valid).isTrue()
        assertThat(VideoPublicationValidator.validate(valid()).reasons).isEmpty()
    }

    @Test
    fun `a missing video file is rejected`() {
        val publication = VideoPublication(videoFile = File(tempDir, "missing.mp4"), title = "t")

        val result = VideoPublicationValidator.validate(publication)

        assertThat(result.valid).isFalse()
        assertThat(result.reasons).anyMatch { it.contains("video file not found") }
    }

    @Test
    fun `a blank title is rejected`() {
        val result = VideoPublicationValidator.validate(
            VideoPublication(videoFile = video(), title = "   "),
        )

        assertThat(result.reasons).anyMatch { it.contains("title must not be blank") }
    }

    @Test
    fun `a title longer than the limit is rejected`() {
        val result = VideoPublicationValidator.validate(
            VideoPublication(videoFile = video(), title = "x".repeat(VideoPublicationLimits.TITLE_MAX_LENGTH + 1)),
        )

        assertThat(result.reasons).anyMatch { it.contains("title exceeds") }
    }

    @Test
    fun `a description longer than the limit is rejected`() {
        val result = VideoPublicationValidator.validate(
            VideoPublication(
                videoFile = video(),
                title = "t",
                description = "x".repeat(VideoPublicationLimits.DESCRIPTION_MAX_LENGTH + 1),
            ),
        )

        assertThat(result.reasons).anyMatch { it.contains("description exceeds") }
    }

    @Test
    fun `a description with forbidden characters is rejected`() {
        val result = VideoPublicationValidator.validate(
            VideoPublication(videoFile = video(), title = "t", description = "a < b > c"),
        )

        assertThat(result.reasons).anyMatch { it.contains("description contains forbidden") }
    }

    @Test
    fun `tags combined over the limit are rejected`() {
        val result = VideoPublicationValidator.validate(valid(tags = listOf("x".repeat(300), "y".repeat(300))))

        assertThat(result.reasons).anyMatch { it.contains("tags exceed") }
    }

    @Test
    fun `a forbidden character in a tag is rejected`() {
        val result = VideoPublicationValidator.validate(valid(tags = listOf("a<b")))

        assertThat(result.reasons).anyMatch { it.contains("contains forbidden characters") }
    }

    @Test
    fun `a blank hashtag is rejected`() {
        val publication = valid().copy(hashtags = listOf(" "))

        val result = VideoPublicationValidator.validate(publication)

        assertThat(result.reasons).anyMatch { it.contains("hashtags must not contain blank") }
    }

    @Test
    fun `fewer than three chapters is rejected`() {
        val result = VideoPublicationValidator.validate(
            valid(chapters = listOf(VideoChapter(0, "a"), VideoChapter(10, "b"))),
        )

        assertThat(result.reasons).anyMatch { it.contains("at least 3") }
    }

    @Test
    fun `a first chapter not at zero is rejected`() {
        val result = VideoPublicationValidator.validate(
            valid(chapters = listOf(VideoChapter(5, "a"), VideoChapter(15, "b"), VideoChapter(25, "c"))),
        )

        assertThat(result.reasons).anyMatch { it.contains("must start at 0") }
    }

    @Test
    fun `non increasing chapter starts are rejected`() {
        val result = VideoPublicationValidator.validate(
            valid(chapters = listOf(VideoChapter(0, "a"), VideoChapter(10, "b"), VideoChapter(10, "c"))),
        )

        assertThat(result.reasons).anyMatch { it.contains("strictly increase") }
    }

    @Test
    fun `a chapter shorter than the minimum is rejected`() {
        val result = VideoPublicationValidator.validate(
            valid(chapters = listOf(VideoChapter(0, "a"), VideoChapter(5, "b"), VideoChapter(20, "c"))),
        )

        assertThat(result.reasons).anyMatch { it.contains("minimum is") }
    }

    @Test
    fun `three well spaced chapters are valid`() {
        val result = VideoPublicationValidator.validate(
            valid(chapters = listOf(VideoChapter(0, "a"), VideoChapter(10, "b"), VideoChapter(20, "c"))),
        )

        assertThat(result.valid).isTrue()
    }

    @Test
    fun `a missing thumbnail is rejected`() {
        val publication = valid().copy(thumbnailFile = File(tempDir, "thumb.png"))

        val result = VideoPublicationValidator.validate(publication)

        assertThat(result.reasons).anyMatch { it.contains("thumbnail file not found") }
    }
}
