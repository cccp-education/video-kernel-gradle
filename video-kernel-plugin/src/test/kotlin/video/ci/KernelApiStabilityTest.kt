package video.ci

import video.kernel.AudioPostProduction
import video.kernel.AudioPostSettings
import video.kernel.FfprobeDurationParser
import video.kernel.FileVisualSource
import video.kernel.SubtitleBurnIn
import video.kernel.TextToSpeech
import video.kernel.VideoDurationProbe
import video.kernel.VideoEngine
import video.kernel.VideoOutcome
import video.kernel.VideoProductionEntry
import video.kernel.VideoProductionPlan
import video.kernel.VideoRenderRequest
import video.kernel.VideoRenderResult
import video.kernel.publish.VideoChapter
import video.kernel.publish.VideoLink
import video.kernel.publish.VideoLinks
import video.kernel.publish.VideoPublication
import video.kernel.publish.VideoPublicationResult
import video.kernel.publish.VideoPublisher
import video.kernel.publish.VideoVisibility
import video.kernel.segment.SegmentSource
import video.kernel.segment.VideoScript
import video.kernel.segment.VideoSegment
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Contract-freeze guard (video-kernel 0.5.0).
 *
 * The engine-neutral kernel is a **shared contract** consumed by capsule and
 * hyperframes. It was frozen as 0.1.0 after being proven by both engines (S-018
 * made hyperframes consume the port in production), then promoted to the neutral
 * `video.kernel` package in 0.2.0 (VK-2, breaking). **0.3.0** (VK-4 US-4a)
 * additively promoted the shared duration-probe capability. **0.4.0** (VK-4
 * US-4b→d) additively promotes the shared post-production and narration ports.
 * **0.5.0** (HF-YOUTUBE-PUBLISH US-1) additively promotes the shared video
 * *publication* capability. This guard pins the exported types, the ports and
 * the model properties, so a breaking change to the public surface cannot slip
 * in silently — it must be a deliberate manifest edit.
 *
 * See [KernelApiSurface] for the frozen surface.
 */
class KernelApiStabilityTest {

    @Test
    fun `the exported kernel type set is frozen at 0_5_0`() {
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
                "video.kernel.TextToSpeech",
                "video.kernel.AudioPostProduction",
                "video.kernel.AudioPostSettings",
                "video.kernel.SubtitleBurnIn",
                "video.kernel.publish.VideoPublisher",
                "video.kernel.publish.VideoPublication",
                "video.kernel.publish.VideoPublicationResult",
                "video.kernel.publish.VideoPublicationResult\$Published",
                "video.kernel.publish.VideoPublicationResult\$Degraded",
                "video.kernel.publish.VideoPublicationResult\$Failed",
                "video.kernel.publish.VideoChapter",
                "video.kernel.publish.VideoLink",
                "video.kernel.publish.VideoLinks",
                "video.kernel.publish.VideoVisibility",
                "video.kernel.publish.VideoPublicationLimits",
                "video.kernel.publish.VideoPublicationValidation",
                "video.kernel.publish.VideoPublicationValidator",
                "video.kernel.publish.VideoDescriptionComposer",
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
    fun `the TextToSpeech port is frozen`() {
        assertThat(KernelApiSurface.functionNames(TextToSpeech::class.java))
            .containsExactlyInAnyOrder("synthesize")
    }

    @Test
    fun `the AudioPostProduction port is frozen`() {
        assertThat(KernelApiSurface.functionNames(AudioPostProduction::class.java))
            .containsExactlyInAnyOrder("process")
    }

    @Test
    fun `the SubtitleBurnIn port is frozen`() {
        assertThat(KernelApiSurface.functionNames(SubtitleBurnIn::class.java))
            .containsExactlyInAnyOrder("burnIn")
    }

    @Test
    fun `the VideoPublisher port is frozen`() {
        assertThat(KernelApiSurface.functionNames(VideoPublisher::class.java))
            .containsExactlyInAnyOrder("publish")
    }

    @Test
    fun `the VideoPublicationResult data classes expose the frozen property set`() {
        assertThat(KernelApiSurface.getterNames(VideoPublicationResult.Published::class.java))
            .containsExactlyInAnyOrder("getVideoId", "getUrl")
        assertThat(KernelApiSurface.getterNames(VideoPublicationResult.Degraded::class.java))
            .containsExactlyInAnyOrder("getReason")
        assertThat(KernelApiSurface.getterNames(VideoPublicationResult.Failed::class.java))
            .containsExactlyInAnyOrder("getReason")
    }

    @Test
    fun `the publication model data classes expose the frozen property set`() {
        assertThat(KernelApiSurface.getterNames(VideoPublication::class.java))
            .containsExactlyInAnyOrder(
                "getVideoFile", "getTitle", "getDescription", "getTags", "getHashtags",
                "getChapters", "getLinks", "getVisibility", "getCategoryId",
                "getLanguageCode", "getMadeForKids", "getThumbnailFile", "getPublishAt",
            )

        assertThat(KernelApiSurface.getterNames(VideoChapter::class.java))
            .containsExactlyInAnyOrder("getStartSeconds", "getTitle")

        assertThat(KernelApiSurface.getterNames(VideoLink::class.java))
            .containsExactlyInAnyOrder("getLabel", "getUrl")

        assertThat(KernelApiSurface.getterNames(VideoLinks::class.java))
            .containsExactlyInAnyOrder("getEntries", "getNextVideo", "getPreviousVideo")
    }

    @Test
    fun `the VideoVisibility enum exposes the frozen constants`() {
        assertThat(VideoVisibility.entries.map { it.name })
            .containsExactly("PRIVATE", "UNLISTED", "PUBLIC")
    }

    @Test
    fun `the AudioPostSettings data class exposes the frozen property set`() {
        assertThat(KernelApiSurface.getterNames(AudioPostSettings::class.java))
            .containsExactlyInAnyOrder(
                "getBgmEnabled", "getBgmFile", "getBgmLevelDb", "getLoudnessTargetLufs",
                "getDuckingEnabled", "getTwoPassLoudness",
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
