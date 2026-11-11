package video.auth

/**
 * The identity of the channel a YouTube access token belongs to (HF-YOUTUBE-AUTH US-5).
 *
 * @property id the channel id (`UC…`).
 * @property title the channel display title (diagnostic only, may be blank).
 */
data class YouTubeChannel(
    val id: String,
    val title: String = "",
)

/**
 * Minimal, dependency-free parser of a YouTube Data API `channels.list` response
 * (HF-YOUTUBE-AUTH US-5).
 *
 * Pure — no I/O. The preflight needs exactly two fields (the authenticated
 * channel id and its title), so a hand-rolled extractor keeps the plugin free of
 * a JSON library. The channel id is matched by its `UC` prefix: a `videos.list`
 * response would carry a `videoId`, never a bare `"id":"UC…"`.
 */
object YouTubeChannelProbe {
    private val CHANNEL_ID = Regex("\"id\"\\s*:\\s*\"(UC[^\"]+)\"")
    private val TITLE = Regex("\"title\"\\s*:\\s*\"([^\"]*)\"")

    /** Returns the first channel of [json], or `null` when the response carries none. */
    fun parse(json: String): YouTubeChannel? {
        if (json.isBlank()) return null
        val id =
            CHANNEL_ID
                .find(json)
                ?.groupValues
                ?.get(1)
                ?.takeIf { it.isNotBlank() }
                ?: return null
        val title =
            TITLE
                .find(json)
                ?.groupValues
                ?.get(1)
                .orEmpty()
        return YouTubeChannel(id, title)
    }
}

/**
 * The verdict of a preflight against the configured target channel
 * (HF-YOUTUBE-AUTH US-5).
 *
 * Pure: the comparison is unit-testable without any network. An unconfigured
 * expectation (blank) is never a mismatch — the preflight then only reports the
 * channel the token actually targets.
 */
data class YouTubeChannelCheck(
    val expectedChannelId: String,
    val actual: YouTubeChannel,
) {
    /** `true` when the credentials file (or the extension) declares a target channel. */
    fun isConfigured(): Boolean = expectedChannelId.isNotBlank()

    /** `true` when nothing is configured or the token targets the configured channel. */
    fun matches(): Boolean = !isConfigured() || expectedChannelId == actual.id

    /** Actionable, secret-free one-line diagnosis. */
    fun summary(): String =
        if (!isConfigured()) {
            "authenticated channel '${actual.title}' (${actual.id})"
        } else if (matches()) {
            "channel '${actual.title}' (${actual.id}) matches the configured $expectedChannelId"
        } else {
            "channel mismatch: configured $expectedChannelId but the token targets " +
                "'${actual.title}' (${actual.id})"
        }
}

/**
 * The `application/x-www-form-urlencoded` form of the refresh-token grant
 * (HF-YOUTUBE-AUTH US-5).
 *
 * Pure: the exact wire keys are unit-tested, so the preflight and the token
 * acquisition build the same grant.
 */
object YouTubeRefreshRequest {
    fun form(
        clientId: String,
        clientSecret: String,
        refreshToken: String,
    ): Map<String, String> =
        mapOf(
            "client_id" to clientId,
            "client_secret" to clientSecret,
            "refresh_token" to refreshToken,
            "grant_type" to "refresh_token",
        )
}
