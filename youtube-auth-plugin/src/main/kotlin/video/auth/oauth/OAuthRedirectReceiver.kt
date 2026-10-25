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
 *
 * @param serverFactory seam over server creation (default binds `127.0.0.1:<port>`).
 *   Tests inject an **already-bound** server (port `0`) so the port is known
 *   atomically — no probe/rebind race between the free-port lookup and the bind.
 */
class LoopbackOAuthReceiver(
    private val serverFactory: (Int) -> HttpServer = { port ->
        HttpServer.create(InetSocketAddress("127.0.0.1", port), 0)
    },
) : OAuthRedirectReceiver {

    override fun awaitCode(port: Int, timeoutMillis: Long): String? {
        val server = serverFactory(port)
        val latch = CountDownLatch(1)
        val captured = AtomicReference<String?>(null)
        server.createContext("/") { exchange ->
            val params = parseQuery(exchange.requestURI.rawQuery ?: "")
            val result = params["code"] ?: params["error"]
            // Only a request actually carrying the authorization result (a `code`
            // or an `error`) completes the wait — a stray request without a query
            // (e.g. `/favicon.ico`) must NOT unblock us with a `null` result.
            if (result != null) {
                captured.set(result)
                latch.countDown()
            }
            val html = if (result != null) {
                "<!doctype html><html><body><h2>Authorization received.</h2>" +
                    "<p>You can close this tab and return to the terminal.</p></body></html>"
            } else {
                "<!doctype html><html><body><p>Waiting for the authorization redirect…</p></body></html>"
            }
            val bytes = html.toByteArray(StandardCharsets.UTF_8)
            exchange.responseHeaders.add("Content-Type", "text/html; charset=utf-8")
            exchange.sendResponseHeaders(200, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
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
