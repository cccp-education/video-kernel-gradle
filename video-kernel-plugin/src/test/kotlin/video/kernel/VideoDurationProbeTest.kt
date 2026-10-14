package video.kernel

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

/**
 * Unit tests for the shared duration-probe capability (VK-4 US-4a).
 *
 * The port is a fun interface (SAM) and the parser is pure — no external
 * process, no `ProcessBuilder` (the concrete ffprobe prober is an N2 adapter).
 */
class VideoDurationProbeTest {

    @Test
    fun `the port is a SAM lambda`() {
        val probe = VideoDurationProbe { 12.5 }
        assertThat(probe.probeSeconds(File("anything.mp4"))).isEqualTo(12.5)
    }

    // ── FfprobeDurationParser — pure ─────────────────────────────────────────

    @Test
    fun `parses a plain decimal line`() {
        assertThat(FfprobeDurationParser.parse("12.5\n")).isEqualTo(12.5)
    }

    @Test
    fun `parses an integer duration`() {
        assertThat(FfprobeDurationParser.parse("60")).isEqualTo(60.0)
    }

    @Test
    fun `ignores surrounding whitespace and blank lines`() {
        assertThat(FfprobeDurationParser.parse("\n  3.25  \n\n")).isEqualTo(3.25)
    }

    @Test
    fun `empty output is unknown`() {
        assertThat(FfprobeDurationParser.parse("")).isNull()
        assertThat(FfprobeDurationParser.parse("\n  \n")).isNull()
    }

    @Test
    fun `non-numeric output is unknown`() {
        assertThat(FfprobeDurationParser.parse("N/A\n")).isNull()
    }

    @Test
    fun `negative, NaN and Infinity are unknown`() {
        assertThat(FfprobeDurationParser.parse("-1.0")).isNull()
        assertThat(FfprobeDurationParser.parse("NaN")).isNull()
        assertThat(FfprobeDurationParser.parse("Infinity")).isNull()
    }

    @Test
    fun `zero is a valid measured duration`() {
        assertThat(FfprobeDurationParser.parse("0.0")).isEqualTo(0.0)
    }
}
