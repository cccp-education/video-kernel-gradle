package video.kernel.publish

/**
 * Outcome of a [VideoPublisher] attempt (HF-YOUTUBE-PUBLISH US-1).
 *
 * A total sealed result rather than an exception — the publisher contract
 * **never throws**, so a build degrades or fails explicitly:
 * - [Published] — the platform accepted the video; carries the id and URL.
 * - [Degraded] — publication was skipped for a safe reason (feature disabled,
 *   credentials absent, empty video file): the build keeps going, the caller
 *   logs the reason.
 * - [Failed]   — the platform refused the publication (API error, quota,
 *   validation refusal): the caller decides to warn or fail per its `strict`
 *   setting.
 */
sealed interface VideoPublicationResult {

    /** The video was accepted by the platform. */
    data class Published(
        /** Platform-assigned video identifier. */
        val videoId: String,
        /** Publicly reachable URL of the published video. */
        val url: String,
    ) : VideoPublicationResult

    /** Publication was safely skipped ([reason] explains why). */
    data class Degraded(val reason: String) : VideoPublicationResult

    /** The platform refused the publication ([reason] carries the cause). */
    data class Failed(val reason: String) : VideoPublicationResult
}
