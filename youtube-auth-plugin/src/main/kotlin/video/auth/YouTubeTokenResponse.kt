package video.auth

/**
 * The subset of a Google OAuth 2.0 token response this flow cares about
 * (HF-YOUTUBE-AUTH US-1).
 */
data class YouTubeTokenResponse(
    val refreshToken: String?,
    val accessToken: String?,
    val error: String? = null,
    val errorDescription: String? = null,
) {
    /** `true` when a durable refresh token was returned. */
    fun hasRefreshToken(): Boolean = !refreshToken.isNullOrBlank()
}

/**
 * Minimal, dependency-free parser of a Google OAuth token JSON response
 * (HF-YOUTUBE-AUTH US-1).
 *
 * Pure — no I/O. A deliberate hand-rolled extractor (no JSON library) so the
 * plugin carries no extra runtime dependency; the token endpoint returns a flat
 * object whose three relevant fields are simple quoted strings.
 */
object YouTubeTokenParser {

    fun parse(json: String): YouTubeTokenResponse {
        require(json.isNotBlank()) { "token response must not be blank" }
        return YouTubeTokenResponse(
            refreshToken = extract(json, "refresh_token"),
            accessToken = extract(json, "access_token"),
            error = extract(json, "error"),
            errorDescription = extract(json, "error_description"),
        )
    }

    private fun extract(json: String, key: String): String? =
        FIELD_PATTERN(key).find(json)?.groupValues?.get(1)?.takeIf { it.isNotBlank() }

    private fun FIELD_PATTERN(key: String) = Regex("\"$key\"\\s*:\\s*\"([^\"]*)\"")
}
