package video.kernel.publish

import java.io.File

/**
 * The engine-neutral description of a video publication
 * (HF-YOUTUBE-PUBLISH US-1).
 *
 * This is the **uniform input** a [VideoPublisher] adapter accepts, whatever the
 * distribution platform: a produced video file plus the editorial metadata a
 * platform needs (title, description, tags, chapters, links, thumbnail,
 * visibility). It is deliberately **neutral** — no platform API field is named
 * verbatim except through plain, optional strings ([categoryId], [publishAt]).
 *
 * The kernel does **not** validate here: an invalid publication is a legitimate
 * object that [VideoPublicationValidator] reports on, so a caller can build then
 * check without construction-time surprises.
 *
 * @property videoFile     the produced video file to publish (must exist).
 * @property title         the video title.
 * @property description   the base description (before [VideoDescriptionComposer]
 *                         appends chapters, links and hashtags).
 * @property tags          keyword tags (the platform's keyword field).
 * @property hashtags      hashtags rendered in the description (`#…`; the `#`
 *                         is added by [VideoDescriptionComposer] when missing).
 * @property chapters      ordered chapter markers (empty = no chapters).
 * @property links         identity links + series navigation.
 * @property visibility    audience exposure (defaults to [VideoVisibility.PRIVATE]).
 * @property categoryId    platform category identifier as a plain string
 *                         (`""` = adapter default).
 * @property languageCode  BCP-47 language tag (e.g. `"fr"`), `""` = unknown.
 * @property madeForKids   declared audience flag.
 * @property thumbnailFile optional thumbnail image file (`null` = none).
 * @property publishAt     optional ISO-8601 instant to schedule the publication
 *                         (`null` = publish immediately per [visibility]).
 */
data class VideoPublication(
    val videoFile: File,
    val title: String,
    val description: String = "",
    val tags: List<String> = emptyList(),
    val hashtags: List<String> = emptyList(),
    val chapters: List<VideoChapter> = emptyList(),
    val links: VideoLinks = VideoLinks(),
    val visibility: VideoVisibility = VideoVisibility.PRIVATE,
    val categoryId: String = "",
    val languageCode: String = "",
    val madeForKids: Boolean = false,
    val thumbnailFile: File? = null,
    val publishAt: String? = null,
)
