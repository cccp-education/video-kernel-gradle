package video.auth.oauth

import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration

/**
 * Seam over a form-encoded HTTP POST (HF-YOUTUBE-AUTH US-2).
 *
 * Test-injectable (pattern `HttpTransport`): unit tests inject a fake returning a
 * canned token JSON, so the exchange is exercised without the network.
 */
fun interface HttpFormPoster {
    fun post(url: String, form: Map<String, String>): String
}

/** Default [HttpFormPoster] built on the JDK `HttpClient` (no dependency). */
class JdkHttpFormPoster(
    private val timeout: Duration = Duration.ofSeconds(30),
) : HttpFormPoster {

    private val client: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(15))
        .build()

    override fun post(url: String, form: Map<String, String>): String {
        val body = form.entries.joinToString("&") { (key, value) -> "${encode(key)}=${encode(value)}" }
        val request = HttpRequest.newBuilder(URI.create(url))
            .timeout(timeout)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()
        return client.send(request, HttpResponse.BodyHandlers.ofString()).body()
    }

    private fun encode(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8)
}

/**
 * Exchanges an authorization `code` for tokens against Google's OAuth 2.0 token
 * endpoint (HF-YOUTUBE-AUTH US-2).
 *
 * Returns the **raw JSON body**; parsing is [video.auth.YouTubeTokenParser]'s job
 * (pure, testable).
 */
class GoogleTokenExchanger(private val poster: HttpFormPoster = JdkHttpFormPoster()) {

    fun exchange(code: String, clientId: String, clientSecret: String, redirectUri: String): String =
        poster.post(
            TOKEN_ENDPOINT,
            mapOf(
                "code" to code,
                "client_id" to clientId,
                "client_secret" to clientSecret,
                "redirect_uri" to redirectUri,
                "grant_type" to "authorization_code",
            ),
        )

    companion object {
        /** Google OAuth 2.0 token endpoint. */
        const val TOKEN_ENDPOINT: String = "https://oauth2.googleapis.com/token"
    }
}
