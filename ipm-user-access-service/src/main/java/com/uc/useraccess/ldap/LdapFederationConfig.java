package com.uc.useraccess.ldap;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.ComponentRepresentation;
import org.springframework.stereotype.Component;

/**
 * Demonstrates programmatic configuration of LDAP user federation.
 * <p>
 * Keycloak exposes an Admin REST API for configuring user federation. The
 * {@link #createProvider()} method illustrates the minimal set of fields needed
 * to register an LDAP provider. Connection details are commented to keep the
 * demo self contained. IPM could use this to allow employees stored in a
 * corporate directory to authenticate to submit proposals.
 */
@Component
public class LdapFederationConfig {

    private final Keycloak keycloak;

    public LdapFederationConfig(Keycloak keycloak) {
        this.keycloak = keycloak;
    }

    public void createProvider() {
        ComponentRepresentation ldap = new ComponentRepresentation();
        ldap.setName("Demo LDAP");
        ldap.setProviderId("ldap");
        ldap.setProviderType("org.keycloak.storage.UserStorageProvider");

        // Example connection properties - adjust to match your LDAP server.
        // When configured, analysts can log in to IPM using their corporate
        // credentials instead of a separate account.
        // ldap.getConfig().put("connectionUrl", List.of("ldap://localhost"));
        // ldap.getConfig().put("usersDn", List.of("ou=users,dc=example,dc=com"));

        keycloak.realm("master").components().add(ldap);
    }
}
