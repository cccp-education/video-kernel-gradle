package video.kernel.publish

/**
 * A [VideoPublisher] that never reaches the network (PUBLISH-ADAPTER-N1,
 * extracted from capsule + hyperframes).
 *
 * Used when publishing is disabled (`publish.enabled = false`, the default) or
 * when credentials are absent: publication is skipped and reported as
 * [VideoPublicationResult.Degraded] so the build keeps going. Economy of ink.
 */
class DisabledVideoPublisher(private val reason: String = "YouTube publication disabled") : VideoPublisher {

    override fun publish(publication: VideoPublication): VideoPublicationResult =
        VideoPublicationResult.Degraded(reason)
}
