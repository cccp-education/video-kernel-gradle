package video.kernel.publish

/**
 * Pure outcome of [VideoPublicationValidator] (HF-YOUTUBE-PUBLISH US-1).
 *
 * A single total descriptor rather than an exception: an invalid publication is
 * a normal data value carrying every reason, so a caller can report the whole
 * list to the author instead of failing on the first one.
 *
 * @property valid   `true` when no rule was violated.
 * @property reasons human-readable rule violations (empty when [valid]).
 */
data class VideoPublicationValidation(
    val valid: Boolean,
    val reasons: List<String>,
) {
    companion object {
        /** The canonical valid outcome. */
        val VALID: VideoPublicationValidation = VideoPublicationValidation(true, emptyList())

        /** An invalid outcome from one or more [reasons]. */
        fun invalid(vararg reasons: String): VideoPublicationValidation =
            VideoPublicationValidation(false, reasons.toList())

        /** An invalid outcome from a [reasons] list. */
        fun invalid(reasons: List<String>): VideoPublicationValidation =
            VideoPublicationValidation(false, reasons)
    }
}
