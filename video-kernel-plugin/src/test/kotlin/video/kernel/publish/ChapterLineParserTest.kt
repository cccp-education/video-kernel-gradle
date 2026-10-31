package video.kernel.publish

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * PUBLISH-ADAPTER-N1 — pure chapter-line parser (moved from capsule + hyperframes).
 */
class ChapterLineParserTest {

    @Test
    fun `parses a minute-second timestamp`() {
        assertThat(ChapterLineParser.parseLine("1:30 Setup"))
            .isEqualTo(VideoChapter(90, "Setup"))
    }

    @Test
    fun `parses an hour timestamp`() {
        assertThat(ChapterLineParser.parseLine("1:00:00 Conclusion"))
            .isEqualTo(VideoChapter(3600, "Conclusion"))
    }

    @Test
    fun `parses a first chapter at zero`() {
        assertThat(ChapterLineParser.parseLine("0:00 Introduction"))
            .isEqualTo(VideoChapter(0, "Introduction"))
    }

    @Test
    fun `accepts an equals separator`() {
        assertThat(ChapterLineParser.parseLine("1:30 = Setup"))
            .isEqualTo(VideoChapter(90, "Setup"))
    }

    @Test
    fun `ignores blank and comment lines`() {
        assertThat(ChapterLineParser.parse(listOf("", "   ", "# a heading", "0:00 Intro"))).hasSize(1)
    }

    @Test
    fun `ignores a line without a timestamp`() {
        assertThat(ChapterLineParser.parseLine("Just a title")).isNull()
    }

    @Test
    fun `ignores a line with an empty title`() {
        assertThat(ChapterLineParser.parseLine("1:00")).isNull()
    }
}
