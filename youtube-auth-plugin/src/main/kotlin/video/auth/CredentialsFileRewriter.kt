package video.auth

/**
 * Pure rewriter of the gitignored credentials file (`youtube.yaml`)
 * (HF-YOUTUBE-AUTH US-1).
 *
 * The flow reads the OAuth client id/secret and the loopback wiring from the
 * existing file, then writes the freshly obtained refresh token back **without
 * disturbing any other line** (comments, apiKey, channelId, …). Pure — no I/O;
 * the caller owns file reading/writing.
 */
object CredentialsFileRewriter {

    /** Returns [content] with its `refreshToken:` value set to [refreshToken]. */
    fun withRefreshToken(content: String, refreshToken: String): String {
        require(refreshToken.isNotBlank()) { "refreshToken must not be blank" }
        val trailingNewline = content.endsWith("\n")
        val lines = content.removeSuffix("\n").lines().toMutableList()
        val replacement = "  refreshToken: \"$refreshToken\""
        val index = lines.indexOfFirst { it.trimStart().startsWith("refreshToken:") }
        if (index >= 0) lines[index] = replacement else lines.add(replacement)
        val joined = lines.joinToString("\n")
        return if (trailingNewline) "$joined\n" else joined
    }

    /** Reads the `clientId` value, or `null` when absent/blank. */
    fun clientId(content: String): String? = field(content, "clientId")

    /** Reads the `clientSecret` value, or `null` when absent/blank. */
    fun clientSecret(content: String): String? = field(content, "clientSecret")

    /** Reads the `refreshToken` value, or `null` when absent/blank. */
    fun refreshToken(content: String): String? = field(content, "refreshToken")

    private fun field(content: String, key: String): String? =
        content.lineSequence()
            .map { it.substringBefore('#').trim() }
            .firstOrNull { it.startsWith("$key:") }
            ?.substringAfter(":")
            ?.trim()
            ?.trim('"', '\'')
            ?.takeIf { it.isNotBlank() }
}
