package video.auth

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Builds the Google OAuth 2.0 consent URL of the YouTube refresh-token flow
 * (HF-YOUTUBE-AUTH US-1).
 *
 * Pure — no I/O, no Gradle. The URL is deterministic given its inputs, so it is
 * fully unit-testable; the browser opening and the network calls are N2.
 *
 * The flow requests `access_type=offline` (a refresh token, not only a
 * short-lived access token) and `prompt=consent` (Google only emits the refresh
 * token on a fresh consent).
 */
object YouTubeConsentUrl {

    /** Google OAuth 2.0 authorization endpoint. */
    const val AUTH_ENDPOINT: String = "https://accounts.google.com/o/oauth2/v2/auth"

    /** Scope required to upload a video and set its thumbnail. */
    const val YOUTUBE_UPLOAD_SCOPE: String = "https://www.googleapis.com/auth/youtube.upload"

    /** The loopback redirect URI for a given [port] (must be registered on the OAuth client). */
    fun loopbackRedirect(port: Int): String = "http://localhost:$port/"

    /**
     * Builds the consent URL for [clientId] redirecting to [redirectUri].
     * Callers open it in a browser; Google redirects the authorization `code` to
     * [redirectUri].
     */
    fun build(
        clientId: String,
        redirectUri: String,
        scope: String = YOUTUBE_UPLOAD_SCOPE,
    ): String {
        require(clientId.isNotBlank()) { "clientId must not be blank" }
        require(redirectUri.isNotBlank()) { "redirectUri must not be blank" }
        val params = listOf(
            "client_id" to clientId,
            "redirect_uri" to redirectUri,
            "response_type" to "code",
            "scope" to scope,
            "access_type" to "offline",
            "prompt" to "consent",
        )
        return AUTH_ENDPOINT + "?" + params.joinToString("&") { (key, value) -> "$key=${encode(value)}" }
    }

    private fun encode(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8)
}
