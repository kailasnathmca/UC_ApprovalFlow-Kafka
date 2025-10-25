package com.uc.useraccess.activedirectory;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.ComponentRepresentation;
import org.springframework.stereotype.Component;

/**
 * Similar to {@code LdapFederationConfig} but tailored for Microsoft Active
 * Directory. Keycloak treats Active Directory as an LDAP source with a few
 * additional capabilities. Organisations that already maintain AD can plug it
 * into IPM so employees authenticate with their Windows credentials.
 */
@Component
public class ActiveDirectoryFederationConfig {

    private final Keycloak keycloak;

    public ActiveDirectoryFederationConfig(Keycloak keycloak) {
        this.keycloak = keycloak;
    }

    public void createProvider() {
        ComponentRepresentation ad = new ComponentRepresentation();
        ad.setName("Demo Active Directory");
        ad.setProviderId("ldap");
        ad.setProviderType("org.keycloak.storage.UserStorageProvider");

        // Extra properties such as "allowKerberosAuthentication" could be
        // configured here for Active Directory environments to support IPM's
        // single sign-on for domain-joined machines.

        keycloak.realm("master").components().add(ad);
    }
}
