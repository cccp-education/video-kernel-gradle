package capsule.kernel

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * TDD — engine-neutral [VideoOutcome] descriptor (video-kernel 0.0.3).
 */
class VideoOutcomeTest {

    @Test
    fun `defaults are neutral when a property is unknown`() {
        val outcome = VideoOutcome()

        assertThat(outcome.durationSeconds).isEqualTo(0.0)
        assertThat(outcome.width).isEqualTo(0)
        assertThat(outcome.height).isEqualTo(0)
        assertThat(outcome.sizeBytes).isEqualTo(0L)
        assertThat(outcome.codec).isNull()
    }

    @Test
    fun `carries the produced video properties`() {
        val outcome = VideoOutcome(
            durationSeconds = 30.0,
            width = 1080,
            height = 1920,
            sizeBytes = 4_200_000L,
            codec = "h264",
        )

        assertThat(outcome.durationSeconds).isEqualTo(30.0)
        assertThat(outcome.codec).isEqualTo("h264")
    }
}
