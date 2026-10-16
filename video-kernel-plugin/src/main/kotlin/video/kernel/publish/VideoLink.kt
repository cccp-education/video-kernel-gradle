package video.kernel.publish

/**
 * A labelled hyperlink attached to a published video
 * (HF-YOUTUBE-PUBLISH US-1).
 *
 * Neutral `(label, url)` pair — the kernel does not know which platform renders
 * it, nor whether the platform allows links at all (an adapter decides).
 *
 * @property label human-readable link label (e.g. `"Site"`).
 * @property url   the link target.
 */
data class VideoLink(
    val label: String,
    val url: String,
) {
    init {
        require(label.isNotBlank()) { "link label must not be blank" }
        require(url.isNotBlank()) { "link url must not be blank" }
    }
}

/**
 * The link block of a published video (HF-YOUTUBE-PUBLISH US-1).
 *
 * Groups the persistent identity links ([entries], e.g. the project website,
 * the training catalogue, the author) and the series navigation
 * ([nextVideo] / [previousVideo]). All fields default to empty so a publisher
 * that does not configure links stays backward compatible.
 *
 * @property entries       ordered identity links (website, catalogue, author, …).
 * @property nextVideo     URL of the next video in the series (`""` = none).
 * @property previousVideo URL of the previous video in the series (`""` = none).
 */
data class VideoLinks(
    val entries: List<VideoLink> = emptyList(),
    val nextVideo: String = "",
    val previousVideo: String = "",
) {
    /** `true` when no link at all is configured. */
    fun isEmpty(): Boolean =
        entries.isEmpty() && nextVideo.isBlank() && previousVideo.isBlank()
}
