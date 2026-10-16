package video.kernel.publish

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

/**
 * TDD — pure [VideoDescriptionComposer] (HF-YOUTUBE-PUBLISH US-1).
 */
class VideoDescriptionComposerTest {

    private fun publication(
        description: String = "",
        hashtags: List<String> = emptyList(),
        chapters: List<VideoChapter> = emptyList(),
        links: VideoLinks = VideoLinks(),
    ) = VideoPublication(
        videoFile = File("video.mp4"),
        title = "Title",
        description = description,
        hashtags = hashtags,
        chapters = chapters,
        links = links,
    )

    @Test
    fun `formats a sub-hour timestamp as M colon SS`() {
        assertThat(VideoDescriptionComposer.chapterTimestamp(0)).isEqualTo("0:00")
        assertThat(VideoDescriptionComposer.chapterTimestamp(65)).isEqualTo("1:05")
        assertThat(VideoDescriptionComposer.chapterTimestamp(599)).isEqualTo("9:59")
    }

    @Test
    fun `formats an hour-plus timestamp as H colon MM colon SS`() {
        assertThat(VideoDescriptionComposer.chapterTimestamp(3600)).isEqualTo("1:00:00")
        assertThat(VideoDescriptionComposer.chapterTimestamp(3725)).isEqualTo("1:02:05")
    }

    @Test
    fun `omits every optional section when empty`() {
        assertThat(VideoDescriptionComposer.compose(publication())).isEmpty()
    }

    @Test
    fun `composes header and description`() {
        val text = VideoDescriptionComposer.compose(publication(description = "Body"), header = "Header")

        assertThat(text).isEqualTo("Header\n\nBody")
    }

    @Test
    fun `renders the chapter block with timestamps`() {
        val text = VideoDescriptionComposer.compose(
            publication(
                chapters = listOf(
                    VideoChapter(0, "Intro"),
                    VideoChapter(10, "Setup"),
                    VideoChapter(20, "Outro"),
                ),
            ),
        )

        assertThat(text).isEqualTo(
            "Chapters\n0:00 Intro\n0:10 Setup\n0:20 Outro",
        )
    }

    @Test
    fun `renders identity links then navigation`() {
        val text = VideoDescriptionComposer.compose(
            publication(
                links = VideoLinks(
                    entries = listOf(
                        VideoLink("Site", "https://cccp.education/"),
                        VideoLink("Formations", "https://talaria.school/"),
                        VideoLink("Auteur", "https://cheroliv.com/"),
                    ),
                    previousVideo = "https://youtu.be/prev",
                    nextVideo = "https://youtu.be/next",
                ),
            ),
        )

        assertThat(text).isEqualTo(
            """
            Links
            Site: https://cccp.education/
            Formations: https://talaria.school/
            Auteur: https://cheroliv.com/
            Previous video: https://youtu.be/prev
            Next video: https://youtu.be/next
            """.trimIndent(),
        )
    }

    @Test
    fun `adds the hash prefix to hashtags`() {
        val text = VideoDescriptionComposer.compose(publication(hashtags = listOf("cccp", "#formation")))

        assertThat(text).isEqualTo("#cccp #formation")
    }

    @Test
    fun `strips forbidden characters from the composed description`() {
        val text = VideoDescriptionComposer.compose(publication(description = "a <b> c"))

        assertThat(text).isEqualTo("a b c")
    }

    @Test
    fun `assembles every section in order`() {
        val text = VideoDescriptionComposer.compose(
            publication(
                description = "Body",
                chapters = listOf(
                    VideoChapter(0, "Intro"),
                    VideoChapter(10, "Setup"),
                    VideoChapter(20, "Outro"),
                ),
                links = VideoLinks(entries = listOf(VideoLink("Site", "https://cccp.education/"))),
                hashtags = listOf("cccp"),
            ),
            header = "Header",
        )

        assertThat(text).isEqualTo(
            """
            Header

            Body

            Chapters
            0:00 Intro
            0:10 Setup
            0:20 Outro

            Links
            Site: https://cccp.education/

            #cccp
            """.trimIndent(),
        )
    }
}
