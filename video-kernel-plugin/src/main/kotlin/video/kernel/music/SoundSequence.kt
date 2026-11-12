package video.kernel.music

/**
 * One sound declaration of the pilot AsciiDoc, attached to a scene
 * (HF-MUSIC-LIBRARY US-2 / D12).
 *
 * The cue is authored in the document (`[.hyperframes-music]`) and carries the
 * neutral intent; resolution against the library happens later. A scene without
 * a cue inherits the running music (the sequence is document-ordered) — there is
 * no implicit "silence".
 *
 * @property sceneId the scene the cue is attached to.
 * @property trackId explicit track id (blank = select by [mood]).
 * @property mood aesthetic intent used when [trackId] is blank.
 * @property volume perceived level in `0..1`.
 * @property fadeInSeconds fade-in length (>= 0).
 * @property fadeOutSeconds fade-out length (>= 0).
 * @property loop loop the track across the scene (default `true`).
 * @property stop explicitly stop the running music at this scene.
 */
data class SoundCue(
    val sceneId: String,
    val trackId: String = "",
    val mood: String = "",
    val volume: Double = 0.25,
    val fadeInSeconds: Double = 0.0,
    val fadeOutSeconds: Double = 0.0,
    val loop: Boolean = true,
    val stop: Boolean = false,
) {
    init {
        require(sceneId.isNotBlank()) { "sceneId must not be blank" }
        require(volume.isFinite() && volume in 0.0..1.0) { "volume must be in 0..1, got $volume" }
        require(fadeInSeconds.isFinite() && fadeInSeconds >= 0.0) {
            "fadeInSeconds must be a finite non-negative value, got $fadeInSeconds"
        }
        require(fadeOutSeconds.isFinite() && fadeOutSeconds >= 0.0) {
            "fadeOutSeconds must be a finite non-negative value, got $fadeOutSeconds"
        }
        require(stop || trackId.isNotBlank() || mood.isNotBlank()) {
            "a sound cue must declare a trackId, a mood, or stop=true"
        }
    }
}

/**
 * The ordered sound cues of a composition, in document order
 * (HF-MUSIC-LIBRARY US-2).
 */
data class SoundSequence(
    val cues: List<SoundCue> = emptyList(),
) {
    /** `true` when there is no cue at all (no sound layer). */
    fun isEmpty(): Boolean = cues.isEmpty()

    /** Cues in document order (the constructor order is the timeline order). */
    fun ordered(): List<SoundCue> = cues
}

/**
 * Verdict of the sound-sequence validation (HF-MUSIC-LIBRARY US-2).
 */
sealed interface SoundSequenceValidation {
    data object Valid : SoundSequenceValidation

    data class Invalid(
        val reasons: List<String>,
    ) : SoundSequenceValidation
}

/**
 * Pure validation of a sound sequence against a music library
 * (HF-MUSIC-LIBRARY US-2).
 *
 * Fails **before** any render so the author sees an actionable message instead
 * of a silent video: unknown track, unusable licence, no match for a mood, or
 * two cues competing for the same scene. `stop` cues are always valid.
 */
object SoundSequenceValidator {
    fun validate(
        sequence: SoundSequence,
        library: MusicLibrary,
    ): SoundSequenceValidation {
        val reasons = mutableListOf<String>()
        val seenScenes = mutableSetOf<String>()
        sequence.cues.forEach { cue ->
            if (!seenScenes.add(cue.sceneId)) {
                reasons += "duplicate sound cue for scene '${cue.sceneId}'"
                return@forEach
            }
            if (cue.stop) return@forEach
            when (val selection = MusicTrackSelector.select(library, cue.trackId, cue.mood)) {
                is MusicSelection.Selected -> Unit
                is MusicSelection.Rejected -> reasons += "scene '${cue.sceneId}': ${selection.reason}"
            }
        }
        return if (reasons.isEmpty()) SoundSequenceValidation.Valid else SoundSequenceValidation.Invalid(reasons)
    }
}
