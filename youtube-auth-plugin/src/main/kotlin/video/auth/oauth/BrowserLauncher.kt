package video.auth.oauth

import java.awt.Desktop
import java.net.URI

/**
 * Opens a URL in the platform default browser (HF-YOUTUBE-AUTH US-2).
 *
 * Headless-safe: returns `false` (never throws) when no desktop is available —
 * the caller then prints the URL for the user to open manually.
 */
object BrowserLauncher {

    fun open(url: String): Boolean = try {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(URI.create(url))
            true
        } else {
            false
        }
    } catch (e: Exception) {
        false
    }
}
