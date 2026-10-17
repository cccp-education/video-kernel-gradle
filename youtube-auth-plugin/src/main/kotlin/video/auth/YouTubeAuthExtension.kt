package video.auth

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import video.auth.oauth.GoogleTokenExchanger
import video.auth.oauth.LoopbackOAuthReceiver
import video.auth.oauth.OAuthRedirectReceiver

/**
 * DSL extension `youtubeAuth { }` of the YouTube Auth plugin
 * (HF-YOUTUBE-AUTH US-2).
 *
 * @property credentialsFile gitignored `youtube.yaml` the token is written into
 *                           (default `youtube.yaml`, resolved against the project dir).
 * @property port            loopback port (default `8765`); the matching
 *                           `http://localhost:8765/` redirect URI must be
 *                           registered on the OAuth client.
 * @property scope           OAuth scope (default `…/auth/youtube.upload`).
 * @property openBrowser     open the consent URL in the default browser (default `true`).
 * @property timeoutSeconds  how long to wait for the redirect (default `300`).
 * @property receiver        redirect-receiver seam (test-injectable).
 * @property exchanger       token-exchange seam (test-injectable).
 */
abstract class YouTubeAuthExtension {

    abstract val credentialsFile: Property<String>

    abstract val port: Property<Int>

    abstract val scope: Property<String>

    abstract val openBrowser: Property<Boolean>

    abstract val timeoutSeconds: Property<Long>

    @get:Internal
    abstract val receiver: Property<OAuthRedirectReceiver>

    @get:Internal
    abstract val exchanger: Property<GoogleTokenExchanger>

    init {
        credentialsFile.convention("youtube.yaml")
        port.convention(8765)
        scope.convention(YouTubeConsentUrl.YOUTUBE_UPLOAD_SCOPE)
        openBrowser.convention(true)
        timeoutSeconds.convention(300L)
        receiver.convention(LoopbackOAuthReceiver())
        exchanger.convention(GoogleTokenExchanger())
    }
}
