package video.auth.oauth

import java.awt.Desktop
import java.io.File
import java.net.URI
import java.util.UUID

/**
 * Options for opening the Google consent URL in a browser (HF-YOUTUBE-AUTH US-4).
 *
 * @property executable    explicit Chrome/Chromium binary; blank/null auto-detects.
 * @property profileDir    base directory of the isolated browser profile.
 * @property freshProfile  create a brand-new session directory (no cookies) on every
 *                         launch (default `true`). Set to `false` to reuse the isolated
 *                         profile — an account already signed in there is kept.
 * @property extraArgs     additional Chromium command-line arguments.
 */
data class BrowserLaunchOptions(
    val executable: String? = null,
    val profileDir: File? = null,
    val freshProfile: Boolean = true,
    val extraArgs: List<String> = emptyList(),
)

/**
 * Result of an attempt to open the consent URL (HF-YOUTUBE-AUTH US-4).
 */
sealed interface BrowserLaunchResult {
    /**
     * The URL was opened in a Chromium-based browser with a dedicated profile — it
     * carries **none** of the cookies of the default browser profile, so Google shows
     * its account picker instead of silently reusing the session currently open.
     */
    data class OpenedInIsolatedProfile(
        val executable: String,
        val profileDir: File,
        val ephemeral: Boolean,
    ) : BrowserLaunchResult

    /** No Chromium binary was found: the URL was handed to the platform default browser. */
    data class OpenedInDefaultBrowser(
        val browser: String?,
    ) : BrowserLaunchResult

    /** Neither a Chromium binary nor a desktop browser is available. */
    data object Unavailable : BrowserLaunchResult
}

/**
 * Builds the Chromium command line (pure — no I/O, no Gradle).
 *
 * A dedicated `--user-data-dir` is what isolates the session: Chromium starts a new
 * process on that directory and never reads the cookies of the default profile, so
 * the consent flow cannot be answered with an already signed-in (wrong) Google
 * account.
 */
object ChromiumLaunchCommand {
    const val USER_DATA_DIR: String = "--user-data-dir"

    /** Flags that keep the fresh profile from stopping on first-run / default-browser prompts. */
    val ISOLATION_FLAGS: List<String> =
        listOf(
            "--no-first-run",
            "--no-default-browser-check",
            "--disable-search-engine-choice-screen",
            "--new-window",
        )

    /**
     * Builds the argv for [executable] with [profileDir] as isolated profile and
     * [url] as the last argument.
     */
    fun build(
        executable: String,
        profileDir: File,
        url: String,
        extraArgs: List<String> = emptyList(),
    ): List<String> {
        require(executable.isNotBlank()) { "executable must not be blank" }
        require(url.isNotBlank()) { "url must not be blank" }
        return buildList {
            add(executable)
            add("$USER_DATA_DIR=${profileDir.absolutePath}")
            addAll(ISOLATION_FLAGS)
            addAll(extraArgs)
            add(url)
        }
    }
}

/**
 * Resolves the Chromium-based browser binary to launch (HF-YOUTUBE-AUTH US-4).
 *
 * Pure with respect to [resolve]: the existence check is injected, so the precedence
 * is unit-testable without touching the host.
 */
object BrowserExecutableResolver {
    private val LINUX_CANDIDATES: List<String> =
        listOf(
            "google-chrome",
            "google-chrome-stable",
            "chromium",
            "chromium-browser",
            "brave-browser",
            "microsoft-edge",
            "vivaldi",
        )

    private val MAC_CANDIDATES: List<String> =
        listOf(
            "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome",
            "/Applications/Chromium.app/Contents/MacOS/Chromium",
            "/Applications/Brave Browser.app/Contents/MacOS/Brave Browser",
            "/Applications/Microsoft Edge.app/Contents/MacOS/Microsoft Edge",
            "/Applications/Vivaldi.app/Contents/MacOS/Vivaldi",
        )

    /** Candidates for the host OS (or [osName], for tests). */
    fun candidates(osName: String = System.getProperty("os.name").orEmpty()): List<String> =
        when {
            osName.startsWith("Mac", ignoreCase = true) -> MAC_CANDIDATES
            osName.startsWith("Windows", ignoreCase = true) -> windowsCandidates()
            else -> LINUX_CANDIDATES
        }

    /**
     * Returns the configured binary when it is executable, otherwise the first
     * executable candidate. Never falls back to a non-executable entry.
     */
    fun resolve(
        configured: String?,
        candidates: List<String>,
        isExecutable: (String) -> Boolean,
    ): String? {
        val ordered =
            buildList {
                configured?.trim()?.takeIf { it.isNotEmpty() }?.let(::add)
                addAll(candidates)
            }
        return ordered.firstOrNull(isExecutable)
    }

    /** Default probe: absolute paths are checked directly, bare names are looked up on `PATH`. */
    fun defaultProbe(candidate: String): Boolean {
        val file = File(candidate)
        if (file.isAbsolute) return file.canExecute()
        return onPath(candidate)
    }

    private fun onPath(name: String): Boolean =
        System
            .getenv("PATH")
            .orEmpty()
            .split(File.pathSeparator)
            .filter { it.isNotBlank() }
            .any { directory -> File(directory, name).canExecute() }

    private fun windowsCandidates(): List<String> {
        val roots =
            listOfNotNull(
                System.getenv("LOCALAPPDATA"),
                System.getenv("ProgramFiles"),
                System.getenv("ProgramFiles(x86)"),
            )
        val absolute =
            roots.flatMap { root ->
                listOf(
                    "$root\\Google\\Chrome\\Application\\chrome.exe",
                    "$root\\Chromium\\Application\\chrome.exe",
                    "$root\\BraveSoftware\\Brave-Browser\\Application\\brave.exe",
                    "$root\\Microsoft\\Edge\\Application\\msedge.exe",
                )
            }
        return absolute + listOf("chrome.exe", "chromium.exe", "brave.exe", "msedge.exe")
    }
}

/**
 * Opens the Google consent URL in an **isolated** Chromium profile (HF-YOUTUBE-AUTH US-4).
 *
 * Why not the platform default browser: on a machine where the default Chrome profile
 * is already signed in, Google answers the consent request with that account — which is
 * how an upload token ends up on the wrong channel. Launching Chromium on a brand-new
 * `--user-data-dir` guarantees an empty cookie jar, so the user explicitly picks the
 * account that owns the target channel.
 *
 * The default browser is only a last resort (headless host, no Chromium installed) and
 * the caller must warn the user in that case.
 */
object BrowserLauncher {
    /**
     * Opens [url] with the host browser, using a fresh isolated profile by default.
     *
     * @param url     the consent URL to open
     * @param options browser selection and profile isolation options
     */
    fun open(
        url: String,
        options: BrowserLaunchOptions = BrowserLaunchOptions(),
    ): BrowserLaunchResult =
        open(
            url = url,
            options = options,
            candidates = BrowserExecutableResolver.candidates(),
            probe = BrowserExecutableResolver::defaultProbe,
            launcher = { command ->
                ProcessBuilder(command)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .start()
            },
            desktopOpener = DesktopBrowserOpener::open,
        )

    internal fun open(
        url: String,
        options: BrowserLaunchOptions,
        candidates: List<String>,
        probe: (String) -> Boolean,
        launcher: (List<String>) -> Unit,
        desktopOpener: (String) -> Boolean,
    ): BrowserLaunchResult {
        val executable = BrowserExecutableResolver.resolve(options.executable, candidates, probe)
        if (executable == null) {
            return if (desktopOpener(url)) {
                BrowserLaunchResult.OpenedInDefaultBrowser(null)
            } else {
                BrowserLaunchResult.Unavailable
            }
        }

        val baseDir =
            options.profileDir
                ?: File(System.getProperty("java.io.tmpdir"), "youtube-auth-chrome-profile")
        val profileDir =
            if (options.freshProfile) {
                File(baseDir, "session-${System.currentTimeMillis()}-${UUID.randomUUID().toString().take(8)}")
            } else {
                baseDir
            }
        profileDir.mkdirs()

        val command = ChromiumLaunchCommand.build(executable, profileDir, url, options.extraArgs)
        return try {
            launcher(command)
            BrowserLaunchResult.OpenedInIsolatedProfile(executable, profileDir, options.freshProfile)
        } catch (e: Exception) {
            if (desktopOpener(url)) {
                BrowserLaunchResult.OpenedInDefaultBrowser(executable)
            } else {
                BrowserLaunchResult.Unavailable
            }
        }
    }
}

/** Headless-safe desktop browser opener (never throws). */
internal object DesktopBrowserOpener {
    fun open(url: String): Boolean =
        try {
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
