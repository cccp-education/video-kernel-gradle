package video.auth.oauth

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

/**
 * Status + body of an authenticated JSON `GET` (HF-YOUTUBE-AUTH US-5).
 */
data class JsonGetResponse(
    val statusCode: Int,
    val body: String,
)

/**
 * Seam over an authenticated JSON `GET` (HF-YOUTUBE-AUTH US-5).
 *
 * Test-injectable (pattern `HttpFormPoster`): unit tests and BDD inject a fake
 * returning a canned channel JSON, so the preflight is exercised without the
 * network. Never returns a secret: only the response body of a public channel
 * resource.
 */
fun interface JsonGetTransport {
    fun get(
        url: String,
        bearerToken: String,
    ): JsonGetResponse
}

/** Default [JsonGetTransport] built on the JDK `HttpClient` (no dependency). */
class JdkJsonGetTransport(
    private val timeout: Duration = Duration.ofSeconds(30),
) : JsonGetTransport {
    private val client: HttpClient =
        HttpClient
            .newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build()

    override fun get(
        url: String,
        bearerToken: String,
    ): JsonGetResponse =
        try {
            val request =
                HttpRequest
                    .newBuilder(URI.create(url))
                    .timeout(timeout)
                    .header("Authorization", "Bearer $bearerToken")
                    .header("Accept", "application/json")
                    .GET()
                    .build()
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            JsonGetResponse(response.statusCode(), response.body())
        } catch (e: Exception) {
            JsonGetResponse(statusCode = -1, body = e.message ?: "")
        }
}
