package video.kernel.publish

/**
 * A minimal, engine-neutral HTTP request used by the publication adapters
 * (PUBLISH-ADAPTER-N1, extracted from capsule + hyperframes).
 *
 * Pure data — the concrete transport (network) lives in the N2 adapters.
 *
 * @property method  HTTP verb (`GET`, `POST`, `PUT`).
 * @property url     absolute request URL.
 * @property headers request headers.
 * @property body    raw request body bytes (empty for a body-less request).
 */
data class PublishHttpRequest(
    val method: String,
    val url: String,
    val headers: Map<String, String> = emptyMap(),
    val body: ByteArray = ByteArray(0),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PublishHttpRequest) return false
        return method == other.method && url == other.url &&
            headers == other.headers && body.contentEquals(other.body)
    }

    override fun hashCode(): Int =
        ((method.hashCode() * 31 + url.hashCode()) * 31 + headers.hashCode()) * 31 + body.contentHashCode()
}

/**
 * Outcome of a [PublishHttpRequest]: status code and response body (text).
 * Response headers are exposed because the resumable upload session URL is
 * returned in the `Location` header of the init response.
 */
data class PublishHttpResponse(
    val statusCode: Int,
    val body: String = "",
    val headers: Map<String, String> = emptyMap(),
)

/**
 * Seam over HTTP — test-injectable. The concrete transport is an N2 adapter;
 * unit tests inject a fake that returns canned responses, so **no test ever
 * touches the network**.
 */
fun interface HttpTransport {
    fun send(request: PublishHttpRequest): PublishHttpResponse
}
