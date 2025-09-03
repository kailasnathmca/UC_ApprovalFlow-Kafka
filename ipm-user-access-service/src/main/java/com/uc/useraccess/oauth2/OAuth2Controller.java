package com.uc.useraccess.oauth2;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Demonstrates a resource that is protected via OAuth 2.0 access tokens.
 * In a real application the Keycloak Spring Security adapter would validate
 * the bearer token before this endpoint is invoked.
 */
@RestController
public class OAuth2Controller {

    @GetMapping("/oauth2/protected")
    public String protectedResource() {
        // Only requests presenting a valid OAuth2 access token issued by
        // Keycloak should be able to call this method.
        return "OAuth2 secured resource";
    }
}
