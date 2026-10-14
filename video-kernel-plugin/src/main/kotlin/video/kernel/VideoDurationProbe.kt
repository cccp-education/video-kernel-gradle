package video.kernel

import java.io.File

/**
 * Engine-neutral port that measures the duration of a produced video, in
 * seconds (EPIC VK-4 US-4a).
 *
 * **Shared capability.** Every engine produces a video whose duration a generic
 * consumer (orchestrator, QA, chapters, N3) needs to know — but reporting it is
 * not the [VideoEngine] contract's job (a whole-document renderer's CLI may not
 * report it at all). Measurement is therefore delegated behind this minimal
 * port, so any engine (Reveal.js capture, HyperFrames document renderer, a
 * future one) exposes the same neutral capability.
 *
 * This is a **port only**: the concrete prober (`ffprobe` via `ProcessBuilder`)
 * is an N2 implementation (one per engine). The kernel stays free of external
 * process coupling — see `video.ci.KernelBoundaryAudit`.
 *
 * Contract: **never throws**. Returns `0.0` when the duration is unknown (file
 * missing, prober absent, unparsable output) — a build must never fail because
 * an optional prober is unavailable.
 */
fun interface VideoDurationProbe {

    /**
     * @param video the produced video file.
     * @return the duration in seconds, or `0.0` when unknown.
     */
    fun probeSeconds(video: File): Double
}

/**
 * Pure parser (stateless object) of an `ffprobe` duration output (VK-4 US-4a).
 *
 * Shared by every engine's prober so the two consumers (capsule, hyperframes)
 * parse the same way. Target command forms both emit a single decimal line:
 * - `ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 <file>`
 * - `ffprobe -v quiet -show_entries format=duration -of csv=p=0 <file>`
 *
 * @return the duration in seconds if the output is a finite decimal >= 0, else
 *         `null` (unknown — the caller degrades to `0.0`).
 */
object FfprobeDurationParser {

    fun parse(stdout: String): Double? {
        val line = stdout.lineSequence().map { it.trim() }.firstOrNull { it.isNotEmpty() } ?: return null
        val value = line.toDoubleOrNull() ?: return null
        return if (value.isFinite() && value >= 0.0) value else null
    }
}
