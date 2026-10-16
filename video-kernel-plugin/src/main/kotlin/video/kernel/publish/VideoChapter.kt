package video.kernel.publish

/**
 * A single named chapter marker of a published video (HF-YOUTUBE-PUBLISH US-1).
 *
 * Neutral by design: a chapter is a `(start, title)` pair a distribution
 * platform turns into a navigable timeline entry. The kernel does not know the
 * platform's accepted format — the adapter (N2) renders it.
 *
 * Invariants enforced at construction (fail-fast — a malformed chapter can
 * never reach a platform):
 * - [startSeconds] is `>= 0` (the timeline origin);
 * - [title] is not blank.
 *
 * @property startSeconds offset from the start of the video, in seconds.
 * @property title        the chapter label shown on the timeline.
 */
data class VideoChapter(
    val startSeconds: Int,
    val title: String,
) {
    init {
        require(startSeconds >= 0) {
            "chapter startSeconds must be >= 0, was $startSeconds"
        }
        require(title.isNotBlank()) {
            "chapter title must not be blank"
        }
    }
}
