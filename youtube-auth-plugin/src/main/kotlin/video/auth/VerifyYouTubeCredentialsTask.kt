package video.auth

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import video.auth.oauth.GoogleTokenExchanger
import video.auth.oauth.JsonGetTransport
import java.io.File

/**
 * `verifyYouTubeCredentials` — read-only preflight of a YouTube publication
 * credential (HF-YOUTUBE-AUTH US-5).
 *
 * It refreshes the stored token and asks `channels.list?mine=true` which channel
 * that token is bound to, then compares it with the configured target channel.
 * It performs **no upload** and prints no secret: only the channel identity and
 * an actionable diagnosis.
 *
 * This is the gate that would have caught two real blockers before an upload
 * attempt: an empty `refreshToken` (never provisioned) and a token bound to the
 * wrong channel (wrong Google account selected during consent).
 *
 * Fail-soft only when `strict = false`; the default is a hard gate, because a
 * preflight that silently succeeds on a broken credential is worthless.
 *
 * @property credentialsFile gitignored `youtube.yaml` (clientId/clientSecret/refreshToken).
 * @property expectedChannelId optional target channel; blank falls back to the
 *                             file's `channelId` (diagnostic when both are blank).
 * @property strict fail the build on an unusable or mismatched credential.
 * @property outputDir receives `verify.json` (outcome + channel identity).
 */
@DisableCachingByDefault(because = "Calls the Google OAuth/YouTube APIs (network side effect): never cacheable")
abstract class VerifyYouTubeCredentialsTask : DefaultTask() {
    @get:Input
    abstract val credentialsFile: Property<String>

    @get:Input
    @get:Optional
    abstract val expectedChannelId: Property<String>

    @get:Input
    abstract val strict: Property<Boolean>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Internal
    abstract val exchanger: Property<GoogleTokenExchanger>

    @get:Internal
    abstract val channelTransport: Property<JsonGetTransport>

    @TaskAction
    fun verify() {
        val outDir = outputDir.asFile.get().apply { mkdirs() }
        val report = File(outDir, "verify.json")
        val file = project.file(credentialsFile.get())
        if (!file.exists()) {
            gate(report, "credentials file not found: ${file.absolutePath} — create a gitignored youtube.yaml first")
            return
        }

        val content = file.readText()
        val clientId = CredentialsFileRewriter.clientId(content)
        val clientSecret = CredentialsFileRewriter.clientSecret(content)
        val refreshToken = CredentialsFileRewriter.refreshToken(content)
        val missing =
            buildList {
                if (clientId == null) add("clientId")
                if (clientSecret == null) add("clientSecret")
                if (refreshToken == null) add("refreshToken")
            }
        if (missing.isNotEmpty()) {
            gate(report, "credentials incomplete in ${file.name}: missing ${missing.joinToString(", ")}")
            return
        }

        val token = YouTubeTokenParser.parse(exchanger.get().refresh(clientId!!, clientSecret!!, refreshToken!!))
        val accessToken = token.accessToken
        if (accessToken.isNullOrBlank()) {
            gate(
                report,
                "token refresh refused by Google " +
                    "(${token.error ?: "unknown error"}: ${token.errorDescription ?: "no description"})",
            )
            return
        }

        val response = channelTransport.get().get(CHANNELS_LIST_URL, accessToken)
        if (response.statusCode !in 200..299) {
            gate(report, "channels.list refused (status ${response.statusCode})")
            return
        }

        val channel = YouTubeChannelProbe.parse(response.body)
        if (channel == null) {
            gate(report, "channels.list returned no channel")
            return
        }

        val configured =
            expectedChannelId.orNull?.takeIf { it.isNotBlank() }
                ?: CredentialsFileRewriter.channelId(content).orEmpty()
        val check = YouTubeChannelCheck(configured, channel)
        writeReport(
            file = report,
            ok = check.matches(),
            channelId = channel.id,
            channelTitle = channel.title,
            expectedChannelId = configured,
            matches = check.matches(),
            reason = check.summary(),
        )
        if (!check.matches()) {
            val message = "verifyYouTubeCredentials: ${check.summary()}"
            if (strict.get()) throw GradleException(message) else logger.warn(message)
        } else {
            logger.lifecycle("verifyYouTubeCredentials: ${check.summary()}")
        }
    }

    private fun gate(
        report: File,
        reason: String,
    ) {
        writeReport(report, ok = false, reason = reason)
        val message = "verifyYouTubeCredentials: $reason"
        if (strict.get()) throw GradleException(message) else logger.warn(message)
    }

    private fun writeReport(
        file: File,
        ok: Boolean,
        channelId: String = "",
        channelTitle: String = "",
        expectedChannelId: String = "",
        matches: Boolean = false,
        reason: String,
    ) {
        val json =
            buildString {
                append("{")
                append("\"ok\":").append(ok).append(",")
                append("\"channelId\":").append(quote(channelId)).append(",")
                append("\"channelTitle\":").append(quote(channelTitle)).append(",")
                append("\"expectedChannelId\":").append(quote(expectedChannelId)).append(",")
                append("\"matches\":").append(matches).append(",")
                append("\"reason\":").append(quote(reason))
                append("}")
            }
        file.writeText(json)
    }

    private fun quote(value: String): String =
        buildString {
            append('"')
            for (c in value) {
                when (c) {
                    '"' -> append("\\\"")
                    '\\' -> append("\\\\")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    else -> if (c < ' ') append("\\u%04x".format(c.code)) else append(c)
                }
            }
            append('"')
        }

    companion object {
        /** `channels.list` endpoint (the authenticated channel when `mine=true`). */
        const val CHANNELS_LIST_URL: String =
            "https://www.googleapis.com/youtube/v3/channels?part=snippet&mine=true"
    }
}
