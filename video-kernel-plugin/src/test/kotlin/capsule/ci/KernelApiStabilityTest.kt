package capsule.ci

import capsule.kernel.FileVisualSource
import capsule.kernel.VideoEngine
import capsule.kernel.VideoOutcome
import capsule.kernel.VideoProductionEntry
import capsule.kernel.VideoProductionPlan
import capsule.kernel.VideoRenderRequest
import capsule.kernel.VideoRenderResult
import capsule.kernel.segment.SegmentSource
import capsule.kernel.segment.VideoScript
import capsule.kernel.segment.VideoSegment
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Contract-freeze guard (video-kernel 0.1.0).
 *
 * The engine-neutral kernel is a **shared contract** consumed by capsule and
 * hyperframes. It is frozen as 0.1.0 after being proven by both engines (S-018
 * made hyperframes consume the port in production). This guard pins the exported
 * types, the two ports and the model properties, so a breaking change to the
 * public surface cannot slip in silently — it must be a deliberate manifest edit.
 *
 * See [KernelApiSurface] for the frozen surface.
 */
class KernelApiStabilityTest {

    @Test
    fun `the exported kernel type set is frozen at 0_1_0`() {
        assertThat(KernelApiSurface.typeNames())
            .containsExactlyInAnyOrder(
                "capsule.kernel.VideoEngine",
                "capsule.kernel.VideoRenderRequest",
                "capsule.kernel.VideoRenderResult",
                "capsule.kernel.VideoOutcome",
                "capsule.kernel.VisualSource",
                "capsule.kernel.FileVisualSource",
                "capsule.kernel.VideoProductionEntry",
                "capsule.kernel.VideoProductionPlan",
                "capsule.kernel.segment.VideoSegment",
                "capsule.kernel.segment.VideoKind",
                "capsule.kernel.segment.VideoScript",
                "capsule.kernel.segment.SegmentSource",
            )
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
