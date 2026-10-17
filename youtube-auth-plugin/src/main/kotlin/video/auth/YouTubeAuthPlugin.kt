package video.auth

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Gradle plugin `education.cccp.video.auth` (HF-YOUTUBE-AUTH US-2).
 *
 * Registers the `obtainYouTubeRefreshToken` task, which runs the Google OAuth
 * 2.0 loopback flow once and writes the resulting refresh token into the
 * gitignored `youtube.yaml`. It pairs with the N1 `video.kernel.publish`
 * publication port: obtain the token here, then publish with
 * `publishHyperframesToYouTube` / `publishCapsuleToYouTube`.
 *
 * Usage:
 * ```kotlin
 * plugins { id("education.cccp.video.auth") }
 * youtubeAuth {
 *     credentialsFile.set("youtube.yaml")
 *     port.set(8765)
 * }
 * ./gradlew obtainYouTubeRefreshToken
 * ```
 */
class YouTubeAuthPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val extension = target.extensions.create("youtubeAuth", YouTubeAuthExtension::class.java)

        target.tasks.register("obtainYouTubeRefreshToken", ObtainYouTubeRefreshTokenTask::class.java) { task ->
            task.group = "deploy"
            task.description =
                "Obtain a YouTube OAuth 2.0 refresh token via a local loopback flow and write it into the credentials file"
            task.credentialsFile.set(extension.credentialsFile)
            task.port.set(extension.port)
            task.scope.set(extension.scope)
            task.openBrowser.set(extension.openBrowser)
            task.timeoutSeconds.set(extension.timeoutSeconds)
            task.receiver.set(extension.receiver)
            task.exchanger.set(extension.exchanger)
        }
    }
}
