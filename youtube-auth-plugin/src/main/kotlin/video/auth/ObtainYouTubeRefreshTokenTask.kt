package video.auth

import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import video.auth.oauth.BrowserLauncher
import video.auth.oauth.GoogleTokenExchanger
import video.auth.oauth.OAuthRedirectReceiver

/**
 * `obtainYouTubeRefreshToken` — runs the Google OAuth 2.0 loopback flow once and
 * writes the resulting refresh token into the credentials file
 * (HF-YOUTUBE-AUTH US-2).
 *
 * Steps:
 * 1. read `clientId`/`clientSecret` from [credentialsFile];
 * 2. print (and, when enabled, open) the consent URL built by
 *    [YouTubeConsentUrl];
 * 3. block on the loopback redirect ([receiver]) to capture the `code`;
 * 4. exchange the code for tokens ([exchanger]) and parse the response
 *    ([YouTubeTokenParser]);
 * 5. rewrite the `refreshToken:` line ([CredentialsFileRewriter]) — every other
 *    line of the file is preserved.
 *
 * The secrets never appear in the logs. Interactive (never cacheable).
 */
@DisableCachingByDefault(because = "Interactive OAuth flow (browser + loopback): never cacheable")
abstract class ObtainYouTubeRefreshTokenTask : DefaultTask() {

    /** Gitignored credentials file carrying `clientId`/`clientSecret` and receiving the token. */
    @get:Input
    abstract val credentialsFile: Property<String>

    /** Loopback port (its `http://localhost:<port>/` redirect URI must be registered). */
    @get:Input
    abstract val port: Property<Int>

    @get:Input
    @get:Optional
    abstract val scope: Property<String>

    @get:Input
    abstract val openBrowser: Property<Boolean>

    @get:Input
    abstract val timeoutSeconds: Property<Long>

    @get:Internal
    abstract val receiver: Property<OAuthRedirectReceiver>

    @get:Internal
    abstract val exchanger: Property<GoogleTokenExchanger>

    @TaskAction
    fun obtain() {
        val file = project.file(credentialsFile.get())
        if (!file.exists()) {
            throw GradleException(
                "credentials file not found: ${file.absolutePath} — create a gitignored youtube.yaml with " +
                    "your clientId and clientSecret first (see the plugin README).",
            )
        }
        val content = file.readText()
        val clientId = CredentialsFileRewriter.clientId(content)
            ?: throw GradleException("clientId is missing in ${file.name}")
        val clientSecret = CredentialsFileRewriter.clientSecret(content)
            ?: throw GradleException("clientSecret is missing in ${file.name}")

        val redirectUri = YouTubeConsentUrl.loopbackRedirect(port.get())
        val consentUrl = YouTubeConsentUrl.build(clientId, redirectUri, scope.get())

        logger.lifecycle("YouTube Auth — open this URL and grant the scope:")
        logger.lifecycle(consentUrl)
        if (openBrowser.get() && BrowserLauncher.open(consentUrl)) {
            logger.lifecycle("(consent URL opened in the default browser)")
        }
        logger.lifecycle("Waiting for the authorization redirect on $redirectUri …")

        val code = receiver.get().awaitCode(port.get(), timeoutSeconds.get() * 1000L)
            ?: throw GradleException("no authorization code received within ${timeoutSeconds.get()} s")

        val json = exchanger.get().exchange(code, clientId, clientSecret, redirectUri)
        val response = YouTubeTokenParser.parse(json)
        if (!response.hasRefreshToken()) {
            throw GradleException(
                "no refresh token returned by Google " +
                    "(${response.error ?: "unknown error"}: ${response.errorDescription ?: "no description"})",
            )
        }

        val updated = CredentialsFileRewriter.withRefreshToken(content, response.refreshToken!!)
        file.writeText(updated)
        logger.lifecycle("Refresh token written to ${file.absolutePath}")
    }
}
