package com.uc.useraccess.social;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.IdentityProviderRepresentation;
import org.springframework.stereotype.Component;

/**
 * Registers a social login provider such as GitHub or Google with Keycloak.
 * Only the minimal configuration is shown; client ids/secrets would normally
 * be supplied via the {@code config} map on the representation. Allowing
 * social logins helps IPM on-board external consultants that do not belong to
 * the corporate directory.
 */
@Component
public class SocialLoginConfig {

    private final Keycloak keycloak;

    public SocialLoginConfig(Keycloak keycloak) {
        this.keycloak = keycloak;
    }

    public void createProvider() {
        IdentityProviderRepresentation github = new IdentityProviderRepresentation();
        github.setAlias("github");
        github.setProviderId("github");
        // Example: github.getConfig().put("clientId", "your-client-id");
        // After registration, contractors can sign in to IPM with their
        // GitHub account to submit proposals.
        keycloak.realm("master").identityProviders().create(github);
    }
}
