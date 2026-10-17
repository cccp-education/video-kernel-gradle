package video.auth.scenarios

import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import video.auth.CredentialsFileRewriter
import video.auth.YouTubeConsentUrl
import video.auth.YouTubeTokenParser
import video.auth.YouTubeTokenResponse

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

    @When("I build the youtube consent URL for client {string} on port {int}")
    fun buildConsent(clientId: String, port: Int) {
        consentUrl = YouTubeConsentUrl.build(clientId, YouTubeConsentUrl.loopbackRedirect(port))
    }

    @Then("the consent URL targets the Google auth endpoint")
    fun targetsEndpoint() {
        check(consentUrl.startsWith(YouTubeConsentUrl.AUTH_ENDPOINT)) { consentUrl }
    }

    @Then("the consent URL carries query {string} equal to {string}")
    fun queryEquals(key: String, expected: String) {
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
    fun credentialsFile(clientId: String, clientSecret: String) {
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

    private fun query(): Map<String, String> =
        consentUrl.substringAfter("?").split("&").associate {
            val (k, v) = it.split("=", limit = 2)
            URLDecoder.decode(k, StandardCharsets.UTF_8) to URLDecoder.decode(v, StandardCharsets.UTF_8)
        }
}
