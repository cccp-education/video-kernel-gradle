package video.kernel.music

/**
 * One track of the shared music library (HF-MUSIC-LIBRARY US-2).
 *
 * The manifest is produced out-of-band by the `newpipe` borough (extraction +
 * provenance) and consumed by the video engines: the kernel only carries the
 * neutral model and the selection rules. [file] is a **relative** path inside the
 * gitignored audio directory; the audio itself is never published.
 *
 * @property id stable identifier used by the AsciiDoc sound cues.
 * @property title human title (diagnostic/attribution).
 * @property artist author/performer (attribution).
 * @property file relative path of the audio file.
 * @property sha256 content fingerprint (copy skip — economy of ink).
 * @property durationSeconds measured duration (0 when unprobed).
 * @property licence rights under which the track may be used.
 * @property sourceUrl provenance URL (never used at render time).
 * @property attribution credit line required by the licence (may be blank).
 * @property tags free-form aesthetic tags (`piano`, `strings`, `ambient`…).
 * @property mood primary aesthetic intent (`calm`, `epic`, `documentary`…).
 * @property energy intensity in `0..100` (selection hint).
 */
data class MusicTrack(
    val id: String,
    val title: String,
    val artist: String = "",
    val file: String = "",
    val sha256: String = "",
    val durationSeconds: Double = 0.0,
    val licence: MusicLicence = MusicLicence.UNKNOWN,
    val sourceUrl: String = "",
    val attribution: String = "",
    val tags: List<String> = emptyList(),
    val mood: String = "",
    val energy: Int = 0,
) {
    init {
        require(id.isNotBlank()) { "id must not be blank" }
        require(title.isNotBlank()) { "title must not be blank" }
        require(durationSeconds.isFinite() && durationSeconds >= 0.0) {
            "durationSeconds must be a finite non-negative value, got $durationSeconds"
        }
        require(energy in 0..100) { "energy must be in 0..100, got $energy" }
    }

    /**
     * `true` when the track may be selected: a declared, allowed licence **and**
     * an audio file reference.
     */
    fun isUsable(): Boolean = licence != MusicLicence.UNKNOWN && file.isNotBlank()

    /** `true` when the licence obliges an attribution line. */
    fun requiresAttribution(): Boolean = licence.requiresAttribution

    /** `true` when [mood] matches [candidate] (case-insensitive, blank never matches). */
    fun hasMood(candidate: String): Boolean = mood.isNotBlank() && mood.equals(candidate.trim(), ignoreCase = true)
}

/**
 * A set of music tracks with its manifest schema version
 * (HF-MUSIC-LIBRARY US-2).
 *
 * @property schemaVersion manifest schema version (`>= 1`).
 * @property tracks all declared tracks, usable or not.
 */
data class MusicLibrary(
    val schemaVersion: Int = 1,
    val tracks: List<MusicTrack> = emptyList(),
) {
    init {
        require(schemaVersion >= 1) { "schemaVersion must be >= 1, got $schemaVersion" }
    }

    /** The track with [id], or `null`. */
    fun find(id: String): MusicTrack? = tracks.firstOrNull { it.id == id.trim() }

    /** Tracks that may actually be selected (declared licence + audio file). */
    fun usable(): List<MusicTrack> = tracks.filter { it.isUsable() }

    /** Usable tracks whose mood matches [mood]. */
    fun byMood(mood: String): List<MusicTrack> = usable().filter { it.hasMood(mood) }
}
