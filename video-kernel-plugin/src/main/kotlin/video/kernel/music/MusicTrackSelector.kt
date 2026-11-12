package video.kernel.music

/**
 * Outcome of a music selection (HF-MUSIC-LIBRARY US-2).
 */
sealed interface MusicSelection {
    /** A usable track was found. */
    data class Selected(
        val track: MusicTrack,
    ) : MusicSelection

    /** Nothing can be selected; [reason] is actionable and secret-free. */
    data class Rejected(
        val reason: String,
    ) : MusicSelection
}

/**
 * Deterministic track selection (HF-MUSIC-LIBRARY US-2).
 *
 * Pure and LLM-free on purpose: the same document must resolve to the same
 * track. Precedence:
 * 1. an explicit [trackId] (unknown or unusable → rejected, never substituted);
 * 2. a [mood] filter over usable tracks;
 * 3. any usable track.
 *
 * Ties are broken by `id` (stable, locale-independent) so a build is
 * reproducible.
 */
object MusicTrackSelector {
    fun select(
        library: MusicLibrary,
        trackId: String = "",
        mood: String = "",
    ): MusicSelection {
        if (trackId.isNotBlank()) {
            val track =
                library.find(trackId)
                    ?: return MusicSelection.Rejected("unknown track id '${trackId.trim()}'")
            if (!track.isUsable()) {
                return MusicSelection.Rejected(
                    "track '${track.id}' is not usable (licence=${track.licence}, file=${track.file.ifBlank { "none" }})",
                )
            }
            return MusicSelection.Selected(track)
        }

        val candidates =
            if (mood.isNotBlank()) {
                library.byMood(mood).ifEmpty {
                    return MusicSelection.Rejected("no usable track for mood '${mood.trim()}'")
                }
            } else {
                library.usable().ifEmpty {
                    return MusicSelection.Rejected("music library has no usable track")
                }
            }
        return MusicSelection.Selected(candidates.sortedBy { it.id }.first())
    }
}
