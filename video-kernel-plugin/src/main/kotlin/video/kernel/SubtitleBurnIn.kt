package video.kernel

import java.io.File

/**
 * Engine-neutral port that burns a subtitle file into a produced video
 * (EPIC VK-4 US-4c).
 *
 * **Shared capability.** Hard-burning subtitles (SRT/VTT) into a video is a
 * post-production step that applies to the *output* of any engine — a Reveal.js
 * capture engine (capsule-gradle) and a document renderer
 * (hyperframes-gradle/Watts) alike. It belongs to neither engine's render
 * contract, so it is expressed as one minimal neutral port. The concrete
 * implementation (FFmpeg `subtitles` filter) is an **N2 implementation**; the
 * kernel carries no external-process coupling — see `video.ci.KernelBoundaryAudit`.
 *
 * Contract: **never throws**. Returns `true` when [output] was produced,
 * `false` on any degradation (tool absent, missing subtitle file) so the caller
 * keeps the original video instead of failing the build.
 */
fun interface SubtitleBurnIn {

    /**
     * Burns [subtitles] into [video] and writes the result to [output].
     *
     * @param video     the produced video file.
     * @param subtitles the subtitle file (SRT/VTT).
     * @param output    the video file with burned-in subtitles to write.
     * @return `true` when [output] was produced, `false` on degradation.
     */
    fun burnIn(video: File, subtitles: File, output: File): Boolean
}
