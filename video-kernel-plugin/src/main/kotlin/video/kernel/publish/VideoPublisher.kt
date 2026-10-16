package video.kernel.publish

/**
 * Engine-neutral port that publishes a produced video to a distribution
 * platform (HF-YOUTUBE-PUBLISH US-1).
 *
 * **Shared capability.** Every video-producing engine eventually has to publish
 * its output — a Reveal.js capture engine (capsule-gradle) and a document
 * renderer (hyperframes-gradle/Watts) alike. Platform-specific mechanics (OAuth
 * 2.0, resumable upload, thumbnail endpoint, API quotas) are **N2
 * implementations**; the kernel carries no HTTP and no vendor coupling — see
 * `video.ci.KernelBoundaryAudit`.
 *
 * This is a `fun interface`: a publisher is a single behaviour, trivially
 * faked in tests (a lambda returning a canned [VideoPublicationResult]).
 *
 * Contract: **never throws**. Returns a [VideoPublicationResult] describing the
 * outcome — [VideoPublicationResult.Degraded] when publication was safely
 * skipped, [VideoPublicationResult.Failed] when the platform refused it.
 */
fun interface VideoPublisher {

    /**
     * Publishes [publication].
     *
     * @param publication the neutral publication to send to the platform.
     * @return the outcome of the attempt (never `null`, never an exception).
     */
    fun publish(publication: VideoPublication): VideoPublicationResult
}
