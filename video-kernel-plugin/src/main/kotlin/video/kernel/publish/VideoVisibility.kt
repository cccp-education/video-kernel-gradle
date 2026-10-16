package video.kernel.publish

/**
 * The audience a published video is exposed to (HF-YOUTUBE-PUBLISH US-1).
 *
 * Neutral triad shared by every platform that distinguishes a private draft, a
 * link-only reachable upload, and a fully public one. Platform-specific extra
 * states (e.g. scheduled-only) are adapter concerns.
 */
enum class VideoVisibility {
    /** Draft — visible only to the channel owner and its delegates. */
    PRIVATE,

    /** Reachable by link but not listed in searches or the channel page. */
    UNLISTED,

    /** Publicly listed. */
    PUBLIC,
}
