package com.uc.useraccess.mfa;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RequiredActionProviderRepresentation;
import org.springframework.stereotype.Component;

/**
 * Demonstrates enabling Multi-Factor Authentication (MFA) in Keycloak by
 * registering the TOTP required action. Users will be prompted to configure a
 * one-time password generator on their next login.
 */
@Component
public class MfaConfig {

    private final Keycloak keycloak;

    public MfaConfig(Keycloak keycloak) {
        this.keycloak = keycloak;
    }

    public void enableTotp() {
        RequiredActionProviderRepresentation totp = new RequiredActionProviderRepresentation();
        totp.setProviderId("CONFIGURE_TOTP");
        keycloak.realm("master").registerRequiredAction(totp);
    }
}
