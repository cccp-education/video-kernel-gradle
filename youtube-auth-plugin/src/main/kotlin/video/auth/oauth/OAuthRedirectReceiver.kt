package video.auth.oauth

import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * Seam over the loopback redirect receiver (HF-YOUTUBE-AUTH US-2).
 *
 * Test-injectable: the concrete [LoopbackOAuthReceiver] binds a real HTTP
 * listener; unit/functional tests inject a fake that returns a canned code, so
 * the OAuth flow is exercised without a browser.
 */
fun interface OAuthRedirectReceiver {

    /**
     * Blocks until Google redirects the authorization `code` to `http://localhost:port/`.
     *
     * @return the `code` (or the `error` value), or `null` on timeout.
     */
    fun awaitCode(port: Int, timeoutMillis: Long): String?
}

/**
 * A [OAuthRedirectReceiver] backed by the JDK HTTP server (`com.sun.net.httpserver`,
 * no dependency), listening on `127.0.0.1:<port>`.
 */
class LoopbackOAuthReceiver : OAuthRedirectReceiver {

    override fun awaitCode(port: Int, timeoutMillis: Long): String? {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", port), 0)
        val latch = CountDownLatch(1)
        val captured = AtomicReference<String?>(null)
        server.createContext("/") { exchange ->
            val params = parseQuery(exchange.requestURI.rawQuery ?: "")
            captured.set(params["code"] ?: params["error"])
            val html = "<!doctype html><html><body><h2>Authorization received.</h2>" +
                "<p>You can close this tab and return to the terminal.</p></body></html>"
            val bytes = html.toByteArray(StandardCharsets.UTF_8)
            exchange.responseHeaders.add("Content-Type", "text/html; charset=utf-8")
            exchange.sendResponseHeaders(200, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
            latch.countDown()
        }
        server.start()
        val received = try {
            latch.await(timeoutMillis, TimeUnit.MILLISECONDS)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            false
        } finally {
            server.stop(0)
        }
        return if (received) captured.get() else null
    }

    companion object {
        /** Parses a URL query string into a decoded key/value map. */
        fun parseQuery(query: String): Map<String, String> =
            query.split("&")
                .mapNotNull { pair ->
                    val separator = pair.indexOf('=')
                    if (separator < 0) null
                    else decode(pair.substring(0, separator)) to decode(pair.substring(separator + 1))
                }
                .toMap()

        private fun decode(value: String): String = URLDecoder.decode(value, StandardCharsets.UTF_8)
    }
}
