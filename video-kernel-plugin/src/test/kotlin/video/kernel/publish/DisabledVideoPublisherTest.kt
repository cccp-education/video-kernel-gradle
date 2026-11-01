package video.kernel.publish

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

/**
 * PUBLISH-ADAPTER-N1 — network-free degraded publisher (moved from capsule +
 * hyperframes).
 */
class DisabledVideoPublisherTest {
    private fun publication() =
        VideoPublication(
            videoFile = File("video.mp4"),
            title = "Title",
        )

    @Test
    fun `reports a degraded result with the default reason`() {
        val result = DisabledVideoPublisher().publish(publication())

        assertThat(result).isInstanceOf(VideoPublicationResult.Degraded::class.java)
        assertThat((result as VideoPublicationResult.Degraded).reason)
            .isEqualTo("YouTube publication disabled")
    }

    @Test
    fun `reports the caller reason`() {
        val result = DisabledVideoPublisher("credentials missing").publish(publication())

        assertThat((result as VideoPublicationResult.Degraded).reason).isEqualTo("credentials missing")
    }
}
