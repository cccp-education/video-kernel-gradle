package video.ci

import video.kernel.FfprobeDurationParser
import video.kernel.FileVisualSource
import video.kernel.VideoDurationProbe
import video.kernel.VideoEngine
import video.kernel.VideoOutcome
import video.kernel.VideoProductionEntry
import video.kernel.VideoProductionPlan
import video.kernel.VideoRenderRequest
import video.kernel.VideoRenderResult
import video.kernel.segment.SegmentSource
import video.kernel.segment.VideoScript
import video.kernel.segment.VideoSegment
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Contract-freeze guard (video-kernel 0.3.0).
 *
 * The engine-neutral kernel is a **shared contract** consumed by capsule and
 * hyperframes. It was frozen as 0.1.0 after being proven by both engines (S-018
 * made hyperframes consume the port in production), then promoted to the neutral
 * `video.kernel` package in 0.2.0 (VK-2, breaking). **0.3.0** (VK-4 US-4a)
 * additively promotes the shared duration-probe capability
 * ([VideoDurationProbe] + [FfprobeDurationParser]). This guard pins the exported
 * types, the ports and the model properties, so a breaking change to the public
 * surface cannot slip in silently — it must be a deliberate manifest edit.
 *
 * See [KernelApiSurface] for the frozen surface.
 */
class KernelApiStabilityTest {

    @Test
    fun `the exported kernel type set is frozen at 0_3_0`() {
        assertThat(KernelApiSurface.typeNames())
            .containsExactlyInAnyOrder(
                "video.kernel.VideoEngine",
                "video.kernel.VideoRenderRequest",
                "video.kernel.VideoRenderResult",
                "video.kernel.VideoOutcome",
                "video.kernel.VisualSource",
                "video.kernel.FileVisualSource",
                "video.kernel.VideoProductionEntry",
                "video.kernel.VideoProductionPlan",
                "video.kernel.segment.VideoSegment",
                "video.kernel.segment.VideoKind",
                "video.kernel.segment.VideoScript",
                "video.kernel.segment.SegmentSource",
                "video.kernel.VideoDurationProbe",
                "video.kernel.FfprobeDurationParser",
            )
    }

    @Test
    fun `the VideoDurationProbe port is frozen`() {
        assertThat(KernelApiSurface.functionNames(VideoDurationProbe::class.java))
            .containsExactlyInAnyOrder("probeSeconds")
    }

    @Test
    fun `the FfprobeDurationParser exposes only parse`() {
        assertThat(KernelApiSurface.functionNames(FfprobeDurationParser::class.java))
            .containsExactlyInAnyOrder("parse")
    }

    @Test
    fun `the VideoEngine port is frozen`() {
        assertThat(KernelApiSurface.functionNames(VideoEngine::class.java))
            .containsExactlyInAnyOrder("name", "isAvailable", "render", "close")
    }

    @Test
    fun `the SegmentSource port is frozen`() {
        assertThat(KernelApiSurface.functionNames(SegmentSource::class.java))
            .containsExactlyInAnyOrder("name", "segments")
    }

    @Test
    fun `the model data classes expose the frozen property set`() {
        assertThat(KernelApiSurface.getterNames(VideoRenderRequest::class.java))
            .containsExactlyInAnyOrder("getName", "getVisual", "getOutputDir", "getTimeoutMillis")

        assertThat(KernelApiSurface.getterNames(VideoRenderResult::class.java))
            .containsExactlyInAnyOrder("getProduced", "getOutcome", "getFailedUnits")

        assertThat(KernelApiSurface.getterNames(VideoOutcome::class.java))
            .containsExactlyInAnyOrder("getDurationSeconds", "getWidth", "getHeight", "getSizeBytes", "getCodec")

        assertThat(KernelApiSurface.getterNames(FileVisualSource::class.java))
            .containsExactlyInAnyOrder("getFile")

        assertThat(KernelApiSurface.getterNames(VideoProductionEntry::class.java))
            .containsExactlyInAnyOrder("getVisual", "getScriptFile", "getLanguage", "getOutputVideo")

        assertThat(KernelApiSurface.getterNames(VideoProductionPlan::class.java))
            .containsExactlyInAnyOrder("getEntries")

        assertThat(KernelApiSurface.getterNames(VideoSegment::class.java))
            .containsExactlyInAnyOrder(
                "getIndex", "getTitle", "getNarration", "getKind", "getScene",
                "getSpeakerNote", "getManimScene",
            )

        assertThat(KernelApiSurface.getterNames(VideoScript::class.java))
            .containsExactlyInAnyOrder("getName", "getSegments")
    }
}
