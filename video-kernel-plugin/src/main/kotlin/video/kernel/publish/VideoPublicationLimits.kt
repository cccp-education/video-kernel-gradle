package video.kernel.publish

/**
 * Editorial limits a distribution platform enforces on a publication
 * (HF-YOUTUBE-PUBLISH US-1).
 *
 * These are the **neutral editorial bounds** shared by the major video
 * platforms (the YouTube Data API happens to use exactly these numbers): a
 * title of at most 100 characters, a description of at most 5000, tags whose
 * combined length does not exceed 500, and — when chapters are used — at least
 * three of them, the first at the origin, each lasting at least ten seconds.
 *
 * Kept as named constants so the adapter, the validator and the documentation
 * never drift apart.
 */
object VideoPublicationLimits {

    /** Maximum title length, in characters. */
    const val TITLE_MAX_LENGTH: Int = 100

    /** Maximum description length, in characters. */
    const val DESCRIPTION_MAX_LENGTH: Int = 5000

    /** Maximum combined length of every tag, in characters. */
    const val TAGS_TOTAL_MAX_LENGTH: Int = 500

    /** Minimum number of chapter markers when chapters are used. */
    const val MIN_CHAPTERS: Int = 3

    /** Minimum duration of a chapter, in seconds. */
    const val MIN_CHAPTER_DURATION_SECONDS: Int = 10

    /**
     * Characters the platform rejects verbatim in a description
     * (angle brackets — an HTML/XML-escaping hazard). Shared by the validator
     * and [VideoDescriptionComposer].
     */
    val FORBIDDEN_CHARS: Set<Char> = setOf('<', '>')
}
