package video.kernel.publish

/**
 * Composes a platform-ready video description from a [VideoPublication]
 * (HF-YOUTUBE-PUBLISH US-1).
 *
 * Pure — no I/O. Factored from capsule's `VideoDescriptionBuilder` +
 * `YouTubeChaptersFormatter` now that a second engine (hyperframes) needs the
 * same rendering, so both adapters compose descriptions identically.
 *
 * Sections, in order, each separated by a blank line:
 * 1. an optional [header] line;
 * 2. the base [VideoPublication.description];
 * 3. the chapter block (`M:SS title` lines) when chapters are present;
 * 4. the link block (identity links, then next/previous navigation);
 * 5. the hashtag line (`#…`, the `#` added when missing).
 *
 * The composer is **lenient**: it strips every
 * [VideoPublicationLimits.FORBIDDEN_CHARS] character from the result, so a
 * produced description can never be rejected for an angle bracket. Strict
 * rejection is [VideoPublicationValidator]'s job.
 */
object VideoDescriptionComposer {

    /** Section label introducing the chapter timeline. */
    const val CHAPTERS_LABEL: String = "Chapters"

    /** Section label introducing the identity/navigation links. */
    const val LINKS_LABEL: String = "Links"

    /** Composes the full description, free of forbidden characters. */
    fun compose(publication: VideoPublication, header: String = ""): String {
        val blocks = mutableListOf<String>()

        if (header.isNotBlank()) blocks += header.trim()
        if (publication.description.isNotBlank()) blocks += publication.description.trim()

        chapterBlock(publication.chapters)?.let { blocks += it }
        linkBlock(publication.links)?.let { blocks += it }

        val hashtagLine = hashtagLine(publication.hashtags)
        if (hashtagLine.isNotEmpty()) blocks += hashtagLine

        return sanitize(blocks.joinToString("\n\n")).trim()
    }

    /**
     * Formats a chapter start as `M:SS`, or `H:MM:SS` once it reaches one hour —
     * the two forms video platforms accept.
     */
    fun chapterTimestamp(startSeconds: Int): String {
        val total = startSeconds.coerceAtLeast(0)
        val hours = total / 3600
        val minutes = (total % 3600) / 60
        val seconds = total % 60
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%d:%02d", minutes, seconds)
        }
    }

    /** Removes every [VideoPublicationLimits.FORBIDDEN_CHARS] character. */
    fun sanitize(text: String): String =
        text.filterNot { it in VideoPublicationLimits.FORBIDDEN_CHARS }

    private fun chapterBlock(chapters: List<VideoChapter>): String? {
        if (chapters.isEmpty()) return null
        val lines = chapters.joinToString("\n") { "${chapterTimestamp(it.startSeconds)} ${it.title}" }
        return "$CHAPTERS_LABEL\n$lines"
    }

    private fun linkBlock(links: VideoLinks): String? {
        if (links.isEmpty()) return null
        val lines = mutableListOf<String>()
        links.entries.forEach { lines += "${it.label}: ${it.url}" }
        if (links.previousVideo.isNotBlank()) lines += "Previous video: ${links.previousVideo}"
        if (links.nextVideo.isNotBlank()) lines += "Next video: ${links.nextVideo}"
        return "$LINKS_LABEL\n${lines.joinToString("\n")}"
    }

    private fun hashtagLine(hashtags: List<String>): String =
        hashtags
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .joinToString(" ") { if (it.startsWith("#")) it else "#$it" }
}
