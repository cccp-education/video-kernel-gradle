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
import video.kernel.VisualSource
import video.kernel.audio.AudioSilenceDetector
import video.kernel.audio.RmsEnvelope
import video.kernel.audio.SilenceAnalysis
import video.kernel.publish.VideoChapter
import video.kernel.publish.VideoDescriptionComposer
import video.kernel.publish.VideoLink
import video.kernel.publish.VideoLinks
import video.kernel.publish.VideoPublication
import video.kernel.publish.VideoPublicationLimits
import video.kernel.publish.VideoPublicationResult
import video.kernel.publish.VideoPublicationValidation
import video.kernel.publish.VideoPublicationValidator
import video.kernel.publish.VideoPublisher
import video.kernel.publish.VideoVisibility
import video.kernel.segment.SegmentSource
import video.kernel.segment.VideoKind
import video.kernel.segment.VideoScript
import video.kernel.segment.VideoSegment
import java.lang.reflect.Method
import java.lang.reflect.Modifier

/**
 * Frozen public API surface of the engine-neutral kernel (0.6.0).
 *
 * The 0.0.3 contract is proven by two engines (capsule/reveal.js and
 * hyperframes/Watts), frozen as **0.1.0** and promoted to the neutral
 * `video.kernel` package as **0.2.0** (VK-2, breaking). **0.3.0** (VK-4 US-4a)
 * additively promotes the shared duration-probe capability
 * ([VideoDurationProbe] + [FfprobeDurationParser]). **0.4.0** (VK-4 US-4b→d)
 * additively promotes the shared post-production and narration capabilities
 * ([TextToSpeech], [AudioPostProduction] + [AudioPostSettings], [SubtitleBurnIn]).
 * **0.5.0** (HF-YOUTUBE-PUBLISH US-1) additively promotes the shared video
 * *publication* capability ([VideoPublisher] + `video.kernel.publish` model),
 * now that a second engine genuinely consumes it. **0.6.0** (VK-4 US-4e)
 * additively promotes the shared **audio quality** primitives
 * ([RmsEnvelope], [AudioSilenceDetector], [SilenceAnalysis]), consumed by
 * capsule (`verifyCapsuleVoice`) and hyperframes (`generateHyperframesNarration`).
 * Any change to the exported types, the ports, or the model properties is a
 * breaking change that must be intentional (update this manifest in the same
 * commit as the break).
 *
 * Reflection only — no `kotlin-reflect` dependency.
 */
object KernelApiSurface {

    /** Every public type shipped by the kernel. */
    val types: List<Class<*>> = listOf(
        VideoEngine::class.java,
        VideoRenderRequest::class.java,
        VideoRenderResult::class.java,
        VideoOutcome::class.java,
        VisualSource::class.java,
        FileVisualSource::class.java,
        VideoProductionEntry::class.java,
        VideoProductionPlan::class.java,
        VideoSegment::class.java,
        VideoKind::class.java,
        VideoScript::class.java,
        SegmentSource::class.java,
        VideoDurationProbe::class.java,
        FfprobeDurationParser::class.java,
        TextToSpeech::class.java,
        AudioPostProduction::class.java,
        AudioPostSettings::class.java,
        SubtitleBurnIn::class.java,
        VideoPublisher::class.java,
        VideoPublication::class.java,
        VideoPublicationResult::class.java,
        VideoPublicationResult.Published::class.java,
        VideoPublicationResult.Degraded::class.java,
        VideoPublicationResult.Failed::class.java,
        VideoChapter::class.java,
        VideoLink::class.java,
        VideoLinks::class.java,
        VideoVisibility::class.java,
        VideoPublicationLimits::class.java,
        VideoPublicationValidation::class.java,
        VideoPublicationValidator::class.java,
        VideoDescriptionComposer::class.java,
        AudioSilenceDetector::class.java,
        RmsEnvelope::class.java,
        SilenceAnalysis::class.java,
    )

    /** Fully-qualified names of the exported types. */
    fun typeNames(): List<String> = types.map { it.name }

    /** Public function methods (non-getter, non-synthetic), sorted. */
    fun functionNames(type: Class<*>): List<String> =
        publicMethods(type)
            .map { it.name }
            .filterNot { GETTER.matches(it) }
            .distinct()
            .sorted()

    /** Public property getters (`getX`) of a data class, sorted. */
    fun getterNames(type: Class<*>): List<String> =
        publicMethods(type)
            .map { it.name }
            .filter { GETTER.matches(it) }
            .distinct()
            .sorted()

    private val GETTER = Regex("^get[A-Z].*")

    private val SYNTHETIC = Regex("^(component\\d+.*|copy.*|equals|hashCode|toString)$")

    /**
     * Public methods only — the frozen surface is the **public** API. Private
     * helpers leaked by codegen (e.g. a Kotlin lambda's `parse$lambda$0`, which
     * is `ACC_PRIVATE, ACC_STATIC, ACC_FINAL` yet **not** synthetic) must never
     * be mistaken for exported members.
     */
    private fun publicMethods(type: Class<*>): List<Method> =
        type.declaredMethods
            .filter { Modifier.isPublic(it.modifiers) }
            .filter { !it.isSynthetic }
            .filterNot { SYNTHETIC.matches(it.name) }
}
