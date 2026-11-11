Feature: HF-YOUTUBE-AUTH — YouTube OAuth refresh-token acquisition (pure domain)

  As a maintainer obtaining a YouTube upload credential
  I want the consent URL, the token parsing and the credential rewrite to be deterministic
  So that the interactive (browser) part is the only non-testable step

  Scenario: The consent URL requests offline YouTube upload access
    When I build the youtube consent URL for client "cid.apps.googleusercontent.com" on port 8765
    Then the consent URL targets the Google auth endpoint
    And the consent URL carries query "access_type" equal to "offline"
    And the consent URL carries query "response_type" equal to "code"
    And the consent URL carries query "scope" equal to "https://www.googleapis.com/auth/youtube.upload"
    And the consent URL carries query "redirect_uri" equal to "http://localhost:8765/"

  Scenario: A token response with a refresh token is usable
    When I parse the youtube token response:
      """
      {"access_token":"ya29.a","refresh_token":"1//0gXYZ","expires_in":3599}
      """
    Then the parsed refresh token is "1//0gXYZ"
    And the parsed response has a refresh token

  Scenario: A token response without a refresh token is not usable
    When I parse the youtube token response:
      """
      {"access_token":"ya29.a","expires_in":3599}
      """
    Then the parsed response has no refresh token

  Scenario: Writing the refresh token preserves the other credentials
    Given a youtube credentials file with clientId "cid" and clientSecret "GOCSPX-secret"
    When I write the refresh token "1//0gNEW" into it
    Then the written credentials still contain "cid"
    And the written credentials still contain "GOCSPX-secret"
    And the written credentials carry refresh token "1//0gNEW"

  Scenario: The consent browser uses a dedicated, cookie-free profile
    When I build the isolated chromium command for url "https://accounts.google.com/o/oauth2/v2/auth"
    Then the browser command carries a dedicated user data directory
    And the browser command disables first run and default browser checks
    And the browser command ends with the consent url

  Scenario: The channel probe extracts the authenticated channel identity
    When I probe the youtube channel from response:
      """
      {"items":[{"id":"UC6KzSrC4wCf-066VwEU5Vww","snippet":{"title":"cccp.education"}}]}
      """
    Then the probed channel id is "UC6KzSrC4wCf-066VwEU5Vww"
    And the probed channel title is "cccp.education"

  Scenario: The refresh-token grant is the preflight wire contract
    When I build the youtube refresh token grant for client "cid"
    Then the grant carries "grant_type" equal to "refresh_token"
    And the grant carries "refresh_token" equal to "1//0gXYZ"

  Scenario: A token bound to the configured channel passes the preflight
    When I check the youtube channel "UC6KzSrC4wCf-066VwEU5Vww" titled "cccp.education" against expected "UC6KzSrC4wCf-066VwEU5Vww"
    Then the youtube channel check passes

  Scenario: A token bound to the wrong channel fails the preflight
    When I check the youtube channel "UCother000000000000000000" titled "Talaria.School" against expected "UC6KzSrC4wCf-066VwEU5Vww"
    Then the youtube channel check fails
    And the youtube channel check mentions "UC6KzSrC4wCf-066VwEU5Vww"
