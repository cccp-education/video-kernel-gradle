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
