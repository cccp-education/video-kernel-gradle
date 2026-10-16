package video.kernel.publish

/**
 * Pure validator of a [VideoPublication] (HF-YOUTUBE-PUBLISH US-1).
 *
 * Encodes every editorial rule a platform enforces **before** any network call,
 * so a malformed publication is rejected with an actionable message instead of
 * a remote 400. Stateless — no I/O, no dependencies.
 *
 * Rules:
 * - the [VideoPublication.videoFile] must exist;
 * - the title is non-blank and at most [VideoPublicationLimits.TITLE_MAX_LENGTH];
 * - the description is at most [VideoPublicationLimits.DESCRIPTION_MAX_LENGTH]
 *   and free of [VideoPublicationLimits.FORBIDDEN_CHARS];
 * - every tag is non-blank, free of forbidden characters, and their combined
 *   length does not exceed [VideoPublicationLimits.TAGS_TOTAL_MAX_LENGTH];
 * - when chapters are used there are at least
 *   [VideoPublicationLimits.MIN_CHAPTERS], the first starts at `0`, starts are
 *   strictly increasing and each chapter lasts at least
 *   [VideoPublicationLimits.MIN_CHAPTER_DURATION_SECONDS] seconds
 *   (the last chapter's duration is unknown and therefore unchecked);
 * - the thumbnail, when present, must exist.
 */
object VideoPublicationValidator {

    /** Validates [publication] and returns the full, ordered list of violations. */
    fun validate(publication: VideoPublication): VideoPublicationValidation {
        val reasons = mutableListOf<String>()

        if (!publication.videoFile.exists()) {
            reasons += "video file not found: ${publication.videoFile.absolutePath}"
        }

        if (publication.title.isBlank()) {
            reasons += "title must not be blank"
        } else if (publication.title.length > VideoPublicationLimits.TITLE_MAX_LENGTH) {
            reasons += "title exceeds ${VideoPublicationLimits.TITLE_MAX_LENGTH} characters " +
                "(was ${publication.title.length})"
        }

        if (publication.description.length > VideoPublicationLimits.DESCRIPTION_MAX_LENGTH) {
            reasons += "description exceeds ${VideoPublicationLimits.DESCRIPTION_MAX_LENGTH} characters " +
                "(was ${publication.description.length})"
        }
        if (publication.description.any { it in VideoPublicationLimits.FORBIDDEN_CHARS }) {
            reasons += "description contains forbidden characters " +
                VideoPublicationLimits.FORBIDDEN_CHARS.joinToString("")
        }

        reasons += validateTags(publication.tags)
        reasons += validateFreeText("hashtag", publication.hashtags)
        reasons += validateChapters(publication.chapters)

        publication.thumbnailFile?.let { thumbnail ->
            if (!thumbnail.exists()) {
                reasons += "thumbnail file not found: ${thumbnail.absolutePath}"
            }
        }

        return if (reasons.isEmpty()) VideoPublicationValidation.VALID
        else VideoPublicationValidation.invalid(reasons)
    }

    private fun validateTags(tags: List<String>): List<String> {
        val reasons = mutableListOf<String>()
        tags.forEach { tag ->
            if (tag.isBlank()) reasons += "tags must not contain blank entries"
            if (tag.any { it in VideoPublicationLimits.FORBIDDEN_CHARS }) {
                reasons += "tag '$tag' contains forbidden characters"
            }
        }
        val total = tags.sumOf { it.length }
        if (total > VideoPublicationLimits.TAGS_TOTAL_MAX_LENGTH) {
            reasons += "tags exceed ${VideoPublicationLimits.TAGS_TOTAL_MAX_LENGTH} combined characters " +
                "(was $total)"
        }
        return reasons
    }

    private fun validateFreeText(kind: String, values: List<String>): List<String> {
        val reasons = mutableListOf<String>()
        values.forEach { value ->
            if (value.isBlank()) reasons += "${kind}s must not contain blank entries"
            if (value.any { it in VideoPublicationLimits.FORBIDDEN_CHARS }) {
                reasons += "$kind '$value' contains forbidden characters"
            }
        }
        return reasons
    }

    private fun validateChapters(chapters: List<VideoChapter>): List<String> {
        if (chapters.isEmpty()) return emptyList()
        val reasons = mutableListOf<String>()

        if (chapters.size < VideoPublicationLimits.MIN_CHAPTERS) {
            reasons += "chapters require at least ${VideoPublicationLimits.MIN_CHAPTERS} entries " +
                "(was ${chapters.size})"
        }
        if (chapters.first().startSeconds != 0) {
            reasons += "first chapter must start at 0 (was ${chapters.first().startSeconds})"
        }
        for (i in 1 until chapters.size) {
            val delta = chapters[i].startSeconds - chapters[i - 1].startSeconds
            if (delta <= 0) {
                reasons += "chapter starts must strictly increase " +
                    "(chapter ${i + 1} at ${chapters[i].startSeconds})"
            } else if (delta < VideoPublicationLimits.MIN_CHAPTER_DURATION_SECONDS) {
                reasons += "chapter ${i} lasts $delta s, minimum is " +
                    "${VideoPublicationLimits.MIN_CHAPTER_DURATION_SECONDS} s"
            }
        }
        return reasons
    }
}
