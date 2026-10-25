package video.auth.oauth

import java.net.ServerSocket
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * TDD — [OAuthRedirectReceiver] + loopback roundtrip (HF-YOUTUBE-AUTH US-2).
 *
 * The roundtrip test binds a real local HTTP listener (JDK `com.sun.net.httpserver`),
 * drives it with a real HTTP GET, and asserts the captured code — no browser, no
 * external network.
 */
class LoopbackOAuthReceiverTest {

    @Test
    fun `parses and decodes a query string`() {
        val params = LoopbackOAuthReceiver.parseQuery("code=4%2F0A&scope=https%3A%2F%2Fexample&state=abc")

        assertThat(params["code"]).isEqualTo("4/0A")
        assertThat(params["scope"]).isEqualTo("https://example")
        assertThat(params["state"]).isEqualTo("abc")
    }

    @Test
    fun `ignores a malformed pair`() {
        assertThat(LoopbackOAuthReceiver.parseQuery("code=ok&garbage")).containsOnlyKeys("code")
    }

    @Test
    fun `captures the authorization code from a real loopback redirect`() {
        val port = ServerSocket(0).use { it.localPort }
        val receiver = LoopbackOAuthReceiver()
        val executor = Executors.newSingleThreadExecutor()
        try {
            val future = executor.submit<String?> { receiver.awaitCode(port, 10_000L) }

            val response = getWithRetry("http://localhost:$port/?code=THE_CODE&scope=x")
            assertThat(response.statusCode()).isEqualTo(200)
            assertThat(future.get(10, TimeUnit.SECONDS)).isEqualTo("THE_CODE")
        } finally {
            executor.shutdownNow()
        }
    }

    @Test
    fun `a request without an authorization result does not end the wait`() {
        val port = ServerSocket(0).use { it.localPort }
        val receiver = LoopbackOAuthReceiver()
        val executor = Executors.newSingleThreadExecutor()
        try {
            val future = executor.submit<String?> { receiver.awaitCode(port, 10_000L) }

            // A favicon-like request (no code/error) must NOT unblock us with a null.
            assertThat(getWithRetry("http://localhost:$port/favicon.ico").statusCode()).isEqualTo(200)
            assertThat(future.isDone).isFalse()

            // The real redirect then unblocks it with the code.
            assertThat(getWithRetry("http://localhost:$port/?code=REAL").statusCode()).isEqualTo(200)
            assertThat(future.get(5, TimeUnit.SECONDS)).isEqualTo("REAL")
        } finally {
            executor.shutdownNow()
        }
    }

    @Test
    fun `a timeout returns null`() {
        val port = ServerSocket(0).use { it.localPort }

        val code = LoopbackOAuthReceiver().awaitCode(port, 200L)

        assertThat(code).isNull()
    }

    private fun getWithRetry(url: String, attempts: Int = 20): HttpResponse<String> {
        val client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build()
        val request = HttpRequest.newBuilder(URI.create(url)).GET().build()
        var last: Exception? = null
        repeat(attempts) {
            try {
                return client.send(request, HttpResponse.BodyHandlers.ofString())
            } catch (e: Exception) {
                last = e
                Thread.sleep(100)
            }
        }
        throw last ?: IllegalStateException("no response")
    }
}
