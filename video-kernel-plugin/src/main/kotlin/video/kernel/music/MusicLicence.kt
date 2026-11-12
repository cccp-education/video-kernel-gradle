package video.kernel.music

/**
 * Rights under which a music track may be used in a produced video
 * (HF-MUSIC-LIBRARY US-2).
 *
 * The library is **fail-closed**: anything that is not an explicitly allowed
 * licence is [UNKNOWN] and can never be selected for a publication. Extracting
 * music from a streaming platform is only lawful for content the owner is
 * allowed to reuse; the licence is therefore a mandatory piece of the model, not
 * a decoration.
 */
enum class MusicLicence {
    /** The producer owns the track (own composition, own recording). */
    OWN,

    /** Public-domain dedication (CC0). */
    CC0,

    /** Creative Commons Attribution: reuse allowed **with attribution**. */
    CC_BY,

    /** Royalty-free licence purchased/free-to-use under its terms. */
    ROYALTY_FREE,

    /** Explicit written licence for this use. */
    LICENSED,

    /** Unknown or undeclared: never usable. */
    UNKNOWN,
    ;

    /** `true` when the licence obliges the video description to credit the author. */
    val requiresAttribution: Boolean get() = this == CC_BY

    companion object {
        /**
         * Parses a declared licence (`own`, `cc0`, `cc-by`, `royalty-free`,
         * `licensed`), case/`_`/`-` insensitive. Anything else is [UNKNOWN].
         */
        fun parse(raw: String): MusicLicence =
            when (raw.trim().lowercase().replace('_', '-')) {
                "own" -> OWN
                "cc0" -> CC0
                "cc-by" -> CC_BY
                "royalty-free" -> ROYALTY_FREE
                "licensed" -> LICENSED
                else -> UNKNOWN
            }
    }
}
