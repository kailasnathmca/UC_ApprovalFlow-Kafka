package com.uc.useraccess.saml;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Placeholder controller that would be protected by SAML 2.0 authentication.
 * Keycloak can act as a SAML identity provider; applications integrate using
 * the SAML bindings provided by Spring Security or other libraries.
 */
@RestController
public class SamlController {

    @GetMapping("/saml/info")
    public String info() {
        // In a real SAML setup the assertion received from Keycloak would be
        // validated before returning this response.
        return "SAML 2.0 demo endpoint";
    }
}
