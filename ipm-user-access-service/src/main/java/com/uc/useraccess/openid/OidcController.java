package com.uc.useraccess.openid;

import java.security.Principal;

import org.keycloak.KeycloakPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Demonstrates how to extract information from the OpenID Connect ID token
 * issued by Keycloak. The {@link KeycloakPrincipal} exposes the ID token and
 * access token of the current user.
 */
@RestController
public class OidcController {

    @GetMapping("/oidc/token")
    public String token(Principal principal) {
        KeycloakPrincipal<?> kp = (KeycloakPrincipal<?>) principal;
        // The ID token follows the OIDC specification and contains standard
        // claims such as subject, issuer and email. Returning the raw token
        // string is sufficient to prove the integration.
        return kp.getKeycloakSecurityContext().getIdTokenString();
    }
}
