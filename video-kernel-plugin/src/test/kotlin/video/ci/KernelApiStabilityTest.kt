package video.ci

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
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
import video.kernel.audio.AudioSilenceDetector
import video.kernel.audio.RmsEnvelope
import video.kernel.audio.SilenceAnalysis
import video.kernel.music.MusicLibrary
import video.kernel.music.MusicLicence
import video.kernel.music.MusicSelection
import video.kernel.music.MusicTrack
import video.kernel.music.MusicTrackSelector
import video.kernel.music.SoundCue
import video.kernel.music.SoundSequenceValidation
import video.kernel.music.SoundSequenceValidator
import video.kernel.notes.SpeakerNoteBlock
import video.kernel.notes.SpeakerNoteBlockParser
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

/**
 * Contract-freeze guard (video-kernel 0.9.0).
 *
 * The engine-neutral kernel is a **shared contract** consumed by capsule and
 * hyperframes. It was frozen as 0.1.0 after being proven by both engines (S-018
 * made hyperframes consume the port in production), then promoted to the neutral
 * `video.kernel` package in 0.2.0 (VK-2, breaking). **0.3.0** (VK-4 US-4a)
 * additively promoted the shared duration-probe capability. **0.4.0** (VK-4
 * US-4b→d) additively promotes the shared post-production and narration ports.
 * **0.5.0** (HF-YOUTUBE-PUBLISH US-1) additively promotes the shared video
 * *publication* capability. **0.6.0** (VK-4 US-4e) additively promotes the
 * shared audio-quality primitives (`RmsEnvelope`, `AudioSilenceDetector`).
 * **0.7.0** (PUBLISH-ADAPTER-N1) additively promotes the shared publication
 * adapter primitives (chapter parser, credentials, command, degraded publisher,
 * HTTP seam). **0.9.0** (HF-SPEAKER-NOTES US-1) additively promotes the shared
 * speaker-note grammar (`SpeakerNoteBlock`, `SpeakerNoteBlockParser`), consumed
 * by capsule's reveal.js adapter and (next) hyperframes' section-based adapter.
 * This guard pins the exported types, the ports and the model properties, so a
 * breaking change to the public surface cannot slip in silently — it must be a
 * deliberate manifest edit.
 *
 * See [KernelApiSurface] for the frozen surface.
 */
class KernelApiStabilityTest {
    @Test
    fun `the exported kernel type set is frozen at 0_9_0`() {
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
                "video.kernel.publish.ChapterLineParser",
                "video.kernel.publish.YouTubeCredentials",
                "video.kernel.publish.YouTubeCredentialsLoader",
                "video.kernel.publish.YouTubePublishCommand",
                "video.kernel.publish.DisabledVideoPublisher",
                "video.kernel.publish.PublishHttpRequest",
                "video.kernel.publish.PublishHttpResponse",
                "video.kernel.publish.HttpTransport",
                "video.kernel.audio.AudioSilenceDetector",
                "video.kernel.audio.RmsEnvelope",
                "video.kernel.audio.SilenceAnalysis",
                "video.kernel.music.MusicLicence",
                "video.kernel.music.MusicTrack",
                "video.kernel.music.MusicLibrary",
                "video.kernel.music.MusicSelection",
                "video.kernel.music.MusicSelection\$Selected",
                "video.kernel.music.MusicSelection\$Rejected",
                "video.kernel.music.MusicTrackSelector",
                "video.kernel.music.SoundCue",
                "video.kernel.music.SoundSequence",
                "video.kernel.music.SoundSequenceValidation",
                "video.kernel.music.SoundSequenceValidation\$Valid",
                "video.kernel.music.SoundSequenceValidation\$Invalid",
                "video.kernel.music.SoundSequenceValidator",
                "video.kernel.notes.SpeakerNoteBlock",
                "video.kernel.notes.SpeakerNoteBlockParser",
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
                "getVideoFile",
                "getTitle",
                "getDescription",
                "getTags",
                "getHashtags",
                "getChapters",
                "getLinks",
                "getVisibility",
                "getCategoryId",
                "getLanguageCode",
                "getMadeForKids",
                "getThumbnailFile",
                "getPublishAt",
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
                "getBgmEnabled",
                "getBgmFile",
                "getBgmLevelDb",
                "getLoudnessTargetLufs",
                "getDuckingEnabled",
                "getTwoPassLoudness",
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
                "getIndex",
                "getTitle",
                "getNarration",
                "getKind",
                "getScene",
                "getSpeakerNote",
                "getManimScene",
            )

        assertThat(KernelApiSurface.getterNames(VideoScript::class.java))
            .containsExactlyInAnyOrder("getName", "getSegments")
    }

    @Test
    fun `the audio silence detector is frozen`() {
        assertThat(KernelApiSurface.functionNames(AudioSilenceDetector::class.java))
            .containsExactlyInAnyOrder("analyze")
    }

    @Test
    fun `the rms envelope is frozen`() {
        assertThat(KernelApiSurface.functionNames(RmsEnvelope::class.java))
            .containsExactlyInAnyOrder("framesFromPcm")
    }

    @Test
    fun `the silence analysis exposes the frozen property set`() {
        assertThat(KernelApiSurface.getterNames(SilenceAnalysis::class.java))
            .containsExactlyInAnyOrder(
                "getTotalFrames",
                "getSilentFrames",
                "getPeakRms",
                "getThreshold",
                "getSilentFraction",
            )
        assertThat(KernelApiSurface.functionNames(SilenceAnalysis::class.java))
            .contains("isSilent")
    }

    @Test
    fun `the music licence enum exposes the frozen constants`() {
        assertThat(MusicLicence.entries.map { it.name })
            .containsExactly("OWN", "CC0", "CC_BY", "ROYALTY_FREE", "LICENSED", "UNKNOWN")
    }

    @Test
    fun `the MusicTrackSelector exposes only select`() {
        assertThat(KernelApiSurface.functionNames(MusicTrackSelector::class.java))
            .containsExactlyInAnyOrder("select")
    }

    @Test
    fun `the music track data class exposes the frozen property set`() {
        assertThat(KernelApiSurface.getterNames(MusicTrack::class.java))
            .containsExactlyInAnyOrder(
                "getId",
                "getTitle",
                "getArtist",
                "getFile",
                "getSha256",
                "getDurationSeconds",
                "getLicence",
                "getSourceUrl",
                "getAttribution",
                "getTags",
                "getMood",
                "getEnergy",
            )
        assertThat(KernelApiSurface.functionNames(MusicTrack::class.java))
            .contains("isUsable", "requiresAttribution", "hasMood")
    }

    @Test
    fun `the music library exposes the frozen property set`() {
        assertThat(KernelApiSurface.getterNames(MusicLibrary::class.java))
            .containsExactlyInAnyOrder("getSchemaVersion", "getTracks")
        assertThat(KernelApiSurface.functionNames(MusicLibrary::class.java))
            .contains("find", "usable", "byMood")
    }

    @Test
    fun `the music selection variants expose the frozen property set`() {
        assertThat(KernelApiSurface.getterNames(MusicSelection.Selected::class.java))
            .containsExactlyInAnyOrder("getTrack")
        assertThat(KernelApiSurface.getterNames(MusicSelection.Rejected::class.java))
            .containsExactlyInAnyOrder("getReason")
    }

    @Test
    fun `the sound cue data class exposes the frozen property set`() {
        assertThat(KernelApiSurface.getterNames(SoundCue::class.java))
            .containsExactlyInAnyOrder(
                "getSceneId",
                "getTrackId",
                "getMood",
                "getVolume",
                "getFadeInSeconds",
                "getFadeOutSeconds",
                "getLoop",
                "getStop",
            )
    }

    @Test
    fun `the sound sequence validation variants expose the frozen property set`() {
        assertThat(KernelApiSurface.getterNames(SoundSequenceValidation.Invalid::class.java))
            .containsExactlyInAnyOrder("getReasons")
        assertThat(KernelApiSurface.functionNames(SoundSequenceValidator::class.java))
            .containsExactlyInAnyOrder("validate")
    }

    @Test
    fun `the speaker note block exposes the frozen property set`() {
        assertThat(KernelApiSurface.getterNames(SpeakerNoteBlock::class.java))
            .containsExactlyInAnyOrder("getTitle", "getText")
    }

    @Test
    fun `the speaker note block parser exposes only parse`() {
        assertThat(KernelApiSurface.functionNames(SpeakerNoteBlockParser::class.java))
            .containsExactlyInAnyOrder("parse")
    }
}
