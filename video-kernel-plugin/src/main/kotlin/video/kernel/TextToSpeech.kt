package video.kernel

import java.io.File

/**
 * Engine-neutral port that synthesizes a narration audio file from a text
 * (EPIC VK-4 US-4d).
 *
 * **Shared capability.** Every narration-oriented engine turns an author's text
 * into spoken audio — a Reveal.js capture engine (capsule-gradle) with a local
 * TTS binary, a document renderer (hyperframes-gradle/Watts) pre-rendering its
 * narration tracks, and any future one. The concrete engine (Piper, espeak, a
 * cloud voice, or a delegating CLI) is an **N2 implementation**; the kernel
 * stays free of external-process and vendor coupling — see
 * `video.ci.KernelBoundaryAudit`.
 *
 * Contract: **never throws**. Returns `true` when [output] was produced and is
 * non-empty, `false` on any degradation (engine absent, synthesis failure) so a
 * build can degrade to a silent/default narration instead of failing.
 *
 * The spoken language is a property of the implementation (a Piper voice or an
 * espeak voice is language-bound), never of this neutral contract.
 */
fun interface TextToSpeech {

    /**
     * Synthesizes [text] into [output].
     *
     * @param text   the narration text to speak.
     * @param output the audio file to write (the engine chooses the container).
     * @return `true` when [output] was produced, `false` on degradation.
     */
    fun synthesize(text: String, output: File): Boolean
}
