package com.uc.useraccess.oauth2;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Demonstrates a resource that is protected via OAuth 2.0 access tokens.
 * In IPM, OAuth2 protects internal APIs such as retrieving sensitive proposal
 * details. A real application would configure the Keycloak Spring Security
 * adapter to validate the bearer token before this endpoint is invoked.
 */
@RestController
public class OAuth2Controller {

    @GetMapping("/oauth2/protected")
    public String protectedResource() {
        // Only requests presenting a valid OAuth2 access token issued by
        // Keycloak should be able to call this method. In IPM this might be
        // used by the front-end to fetch confidential proposal data.
        return "OAuth2 secured resource";
    }
}
