package capsule.kernel

import java.io.File

/**
 * Engine-neutral port that renders a [VisualSource] into a video
 * (video-kernel US-1, 0.0.2).
 *
 * **Shared core.** This port is the common denominator between every video
 * engine: a Reveal.js capture engine (capsule-gradle), a HyperFrames document
 * renderer (hyperframes-gradle/Watts), and any future one. It knows only that a
 * visual source produces a video file. It deliberately knows **nothing** about
 * segments, narration, durations, viewports or capture parallelism — those are
 * capabilities of specific engines (segment-oriented capture), configured on the
 * engine/adapter itself, never imposed on the shared contract.
 *
 * An engine is a stateful resource (a browser, a process pool): callers must
 * invoke [close] once the render is done.
 */
interface VideoEngine {

    /** Human-readable engine name (diagnostics only). */
    fun name(): String

    /** `true` when the engine can actually render (binaries/browser present). */
    fun isAvailable(): Boolean

    /**
     * Renders the production described by [request].
     *
     * Engines report degraded renders through [VideoRenderResult.failedUnits]
     * rather than throwing, so a partial failure never aborts the whole build.
     */
    fun render(request: VideoRenderRequest): VideoRenderResult

    /** Releases the engine's resources (browser, executor, …). Idempotent. */
    fun close()
}

/**
 * A single render instruction — the neutral unit of work handed to a
 * [VideoEngine].
 *
 * Deliberately minimal: engine-specific inputs (segment plan, per-segment
 * durations, viewport, parallelism, …) are supplied when the engine/adapter is
 * built, not here. A segment-oriented capture engine and a whole-document
 * renderer both accept this same request unchanged.
 *
 * @property name          production name (without extension), used to name the
 *                         produced video (`<name>.<ext>`).
 * @property visual        the engine-neutral visual source to render.
 * @property outputDir     directory the engine writes produced files into.
 * @property timeoutMillis optional render budget in milliseconds (`0` = the
 *                         engine's own default).
 */
data class VideoRenderRequest(
    val name: String,
    val visual: VisualSource,
    val outputDir: File,
    val timeoutMillis: Long = 0L,
)

/**
 * Outcome of a [VideoEngine] render.
 *
 * @property produced    the produced video file, or `null` when the engine wrote
 *                       nothing.
 * @property failedUnits engine-reported failed sub-units (indices of the
 *                       segment-oriented capture); empty for a whole-document
 *                       engine. Never fatal — a degraded render still yields
 *                       whatever was produced.
 */
data class VideoRenderResult(
    val produced: File?,
    val failedUnits: List<Int> = emptyList(),
)
