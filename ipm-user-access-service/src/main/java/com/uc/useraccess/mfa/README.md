# Multi-Factor Authentication Demo

`MfaConfig` enables Keycloak's TOTP required action so users must enrol a second factor before accessing IPM.

## Flow
1. `enableTotp()` registers the `CONFIGURE_TOTP` required action.
2. On next login Keycloak prompts users to scan a QR code in an authenticator app.
3. The generated codes are required for future sign-ins to sensitive IPM functions.
