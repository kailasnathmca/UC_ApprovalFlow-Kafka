package com.uc.useraccess.sso;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Demonstrates Single Sign-On (SSO) and Single Sign-Out (SLO).
 * <p>
 * When Spring Security is configured with the Keycloak adapter, a request to
 * any endpoint will redirect the user to Keycloak for authentication. After a
 * successful login, the security context is populated with the user's
 * identity and SSO session. Invoking the logout endpoint would normally
 * trigger Keycloak's logout endpoint which invalidates the session across all
 * participating applications.
 */
@RestController
public class SsoController {

    @GetMapping("/sso")
    public String ssoDemo() {
        // If the request reaches here the user has an active SSO session.
        return "SSO session is active";
    }

    @GetMapping("/logout")
    public String logout() {
        // A real implementation would call the Keycloak logout URL and then
        // redirect the user. We return text for demonstration purposes only.
        return "User logged out of all sessions";
    }
}
