package video.kernel

/**
 * Engine-neutral descriptor of a produced video (video-kernel 0.0.3).
 *
 * The minimal **uniform** view a consumer (orchestrator, QA, chapters, N3) needs
 * to treat any engine's output identically, whatever the engine:
 * - a segment-oriented capture engine (reveal.js/capsule) fills duration + size
 *   from the produced file and dimensions from its capture viewport;
 * - a whole-document renderer (HyperFrames) fills size, dimensions and codec.
 *
 * Fields are total with neutral defaults, so an engine that does not know a
 * property reports `0`/`null` rather than inventing one.
 *
 * @property durationSeconds duration in seconds (`0.0` when unknown).
 * @property width           width in pixels (`0` when unknown).
 * @property height          height in pixels (`0` when unknown).
 * @property sizeBytes       size of the produced file in bytes.
 * @property codec           video codec (`"h264"`, `"vp8"`, …), or `null` when unknown.
 */
data class VideoOutcome(
    val durationSeconds: Double = 0.0,
    val width: Int = 0,
    val height: Int = 0,
    val sizeBytes: Long = 0L,
    val codec: String? = null,
)
