package video.kernel

import java.io.File

/**
 * Engine-neutral port that applies audio post-production to a produced video
 * (EPIC VK-4 US-4b).
 *
 * **Shared capability.** Loudness normalization, a background-music bed, and
 * sidechain ducking are post-production steps that apply to the *output* of any
 * engine — a Reveal.js capture engine (capsule-gradle) and a document renderer
 * (hyperframes-gradle/Watts) alike. They belong to neither engine's render
 * contract, so they are expressed as one minimal neutral port. The concrete
 * implementation (FFmpeg `loudnorm`/`amix`/`sidechaincompress`) is an **N2
 * implementation**; the kernel carries no external-process coupling — see
 * `video.ci.KernelBoundaryAudit`.
 *
 * Contract: **never throws**. Returns `true` when [output] was produced,
 * `false` on any degradation (tool absent, missing background track) so the
 * caller keeps the original video instead of failing the build.
 */
fun interface AudioPostProduction {

    /**
     * Applies post-production to [video] and writes the result to [output].
     *
     * @param video    the produced video file.
     * @param output   the post-processed video file to write.
     * @param settings the neutral post-production settings.
     * @return `true` when [output] was produced, `false` on degradation.
     */
    fun process(video: File, output: File, settings: AudioPostSettings): Boolean
}

/**
 * Neutral audio post-production settings shared by every engine (US-4b).
 *
 * All fields default to disabled/empty so an engine that does not configure
 * audio post keeps its historical behaviour (backward compatibility).
 *
 * @property bgmEnabled           mix a background-music bed under the voice.
 * @property bgmFile              background-music file path (blank = none).
 * @property bgmLevelDb           background-music level under the voice, in dB.
 * @property loudnessTargetLufs   target loudness for normalization, in LUFS.
 * @property duckingEnabled       lower the background bed while the voice speaks.
 * @property twoPassLoudness      use a measure-then-normalize loudness pass.
 */
data class AudioPostSettings(
    val bgmEnabled: Boolean = false,
    val bgmFile: String = "",
    val bgmLevelDb: Double = -18.0,
    val loudnessTargetLufs: Double = -16.0,
    val duckingEnabled: Boolean = false,
    val twoPassLoudness: Boolean = false,
)
