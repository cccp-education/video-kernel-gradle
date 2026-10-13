package capsule.ci

import capsule.kernel.FileVisualSource
import capsule.kernel.VideoEngine
import capsule.kernel.VideoOutcome
import capsule.kernel.VideoProductionEntry
import capsule.kernel.VideoProductionPlan
import capsule.kernel.VideoRenderRequest
import capsule.kernel.VideoRenderResult
import capsule.kernel.VisualSource
import capsule.kernel.segment.SegmentSource
import capsule.kernel.segment.VideoKind
import capsule.kernel.segment.VideoScript
import capsule.kernel.segment.VideoSegment
import java.lang.reflect.Method

/**
 * Frozen public API surface of the engine-neutral kernel (0.1.0).
 *
 * The 0.0.3 contract is proven by two engines (capsule/reveal.js and
 * hyperframes/Watts) and frozen as **0.1.0**: any change to the exported types,
 * the two ports, or the model properties is a breaking change that must be
 * intentional (update this manifest in the same commit as the break).
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

    private fun publicMethods(type: Class<*>): List<Method> =
        type.declaredMethods
            .filter { !it.isSynthetic }
            .filterNot { SYNTHETIC.matches(it.name) }
}
