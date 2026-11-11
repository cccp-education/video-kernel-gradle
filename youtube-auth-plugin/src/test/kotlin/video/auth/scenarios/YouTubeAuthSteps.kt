package video.auth.scenarios

import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import video.auth.CredentialsFileRewriter
import video.auth.YouTubeChannel
import video.auth.YouTubeChannelCheck
import video.auth.YouTubeChannelProbe
import video.auth.YouTubeConsentUrl
import video.auth.YouTubeRefreshRequest
import video.auth.YouTubeTokenParser
import video.auth.YouTubeTokenResponse
import video.auth.oauth.ChromiumLaunchCommand
import java.io.File
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/**
 * Steps for the pure YouTube OAuth domain (HF-YOUTUBE-AUTH US-1).
 *
 * Prefixed (`youtube ...`) to avoid glue collisions. Pure-domain only — the
 * browser/network parts are not exercised here.
 */
class YouTubeAuthSteps {
    private var consentUrl: String = ""
    private var tokenResponse: YouTubeTokenResponse? = null
    private var credentials: String = ""
    private var browserCommand: List<String> = emptyList()
    private var channel: YouTubeChannel? = null
    private var grant: Map<String, String> = emptyMap()
    private var channelCheck: YouTubeChannelCheck? = null

    @When("I build the youtube consent URL for client {string} on port {int}")
    fun buildConsent(
        clientId: String,
        port: Int,
    ) {
        consentUrl = YouTubeConsentUrl.build(clientId, YouTubeConsentUrl.loopbackRedirect(port))
    }

    @Then("the consent URL targets the Google auth endpoint")
    fun targetsEndpoint() {
        check(consentUrl.startsWith(YouTubeConsentUrl.AUTH_ENDPOINT)) { consentUrl }
    }

    @Then("the consent URL carries query {string} equal to {string}")
    fun queryEquals(
        key: String,
        expected: String,
    ) {
        check(query()[key] == expected) { "query '$key' was '${query()[key]}' (expected '$expected')" }
    }

    @When("I parse the youtube token response:")
    fun parseResponse(json: String) {
        tokenResponse = YouTubeTokenParser.parse(json.trim())
    }

    @Then("the parsed refresh token is {string}")
    fun refreshTokenIs(expected: String) {
        check(tokenResponse?.refreshToken == expected) { "was ${tokenResponse?.refreshToken}" }
    }

    @Then("the parsed response has a refresh token")
    fun hasRefreshToken() {
        check(tokenResponse?.hasRefreshToken() == true) { "expected a refresh token" }
    }

    @Then("the parsed response has no refresh token")
    fun hasNoRefreshToken() {
        check(tokenResponse?.hasRefreshToken() == false) { "did not expect a refresh token" }
    }

    @Given("a youtube credentials file with clientId {string} and clientSecret {string}")
    fun credentialsFile(
        clientId: String,
        clientSecret: String,
    ) {
        credentials = "youtube:\n  clientId: \"$clientId\"\n  clientSecret: \"$clientSecret\"\n  refreshToken: \"\"\n"
    }

    @When("I write the refresh token {string} into it")
    fun writeToken(token: String) {
        credentials = CredentialsFileRewriter.withRefreshToken(credentials, token)
    }

    @Then("the written credentials still contain {string}")
    fun stillContains(value: String) {
        check(credentials.contains(value)) { "credentials no longer contain '$value':\n$credentials" }
    }

    @Then("the written credentials carry refresh token {string}")
    fun carriesToken(token: String) {
        check(credentials.contains("refreshToken: \"$token\"")) { credentials }
    }

    @When("I build the isolated chromium command for url {string}")
    fun buildIsolatedBrowserCommand(url: String) {
        browserCommand =
            ChromiumLaunchCommand
                .build("google-chrome", File("/tmp/youtube-auth-profile/session-bdd"), url)
    }

    @Then("the browser command carries a dedicated user data directory")
    fun carriesUserDataDir() {
        check(browserCommand.any { it.startsWith(ChromiumLaunchCommand.USER_DATA_DIR + "=") }) { browserCommand.toString() }
    }

    @Then("the browser command disables first run and default browser checks")
    fun disablesFirstRunPrompts() {
        check(browserCommand.contains("--no-first-run")) { browserCommand.toString() }
        check(browserCommand.contains("--no-default-browser-check")) { browserCommand.toString() }
    }

    @Then("the browser command ends with the consent url")
    fun endsWithConsentUrl() {
        check(browserCommand.lastOrNull()?.startsWith("https://accounts.google.com/") == true) { browserCommand.toString() }
    }

    @When("I probe the youtube channel from response:")
    fun probeChannel(json: String) {
        channel = YouTubeChannelProbe.parse(json.trim())
    }

    @Then("the probed channel id is {string}")
    fun probedChannelId(expected: String) {
        check(channel?.id == expected) { "probed channel id was ${channel?.id}" }
    }

    @Then("the probed channel title is {string}")
    fun probedChannelTitle(expected: String) {
        check(channel?.title == expected) { "probed channel title was ${channel?.title}" }
    }

    @When("I build the youtube refresh token grant for client {string}")
    fun buildRefreshGrant(clientId: String) {
        grant = YouTubeRefreshRequest.form(clientId, "GOCSPX-secret", "1//0gXYZ")
    }

    @Then("the grant carries {string} equal to {string}")
    fun grantCarries(
        key: String,
        expected: String,
    ) {
        check(grant[key] == expected) { "grant '$key' was '${grant[key]}' (expected '$expected')" }
    }

    @When("I check the youtube channel {string} titled {string} against expected {string}")
    fun checkChannel(
        id: String,
        title: String,
        expected: String,
    ) {
        channelCheck = YouTubeChannelCheck(expected, YouTubeChannel(id, title))
    }

    @Then("the youtube channel check passes")
    fun channelCheckPasses() {
        check(channelCheck?.matches() == true) { channelCheck?.summary().orEmpty() }
    }

    @Then("the youtube channel check fails")
    fun channelCheckFails() {
        check(channelCheck?.matches() == false) { channelCheck?.summary().orEmpty() }
    }

    @Then("the youtube channel check mentions {string}")
    fun channelCheckMentions(value: String) {
        check(channelCheck?.summary()?.contains(value) == true) { channelCheck?.summary().orEmpty() }
    }

    private fun query(): Map<String, String> =
        consentUrl.substringAfter("?").split("&").associate {
            val (k, v) = it.split("=", limit = 2)
            URLDecoder.decode(k, StandardCharsets.UTF_8) to URLDecoder.decode(v, StandardCharsets.UTF_8)
        }
}
