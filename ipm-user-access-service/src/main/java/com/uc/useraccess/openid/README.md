# OpenID Connect Demo

`OidcController` demonstrates how IPM retrieves the OpenID Connect ID token issued by Keycloak.

## Flow
1. An authenticated call is made to `/oidc/token`.
2. The controller casts the `Principal` to `KeycloakPrincipal` and returns the raw ID token.
3. IPM services can parse this token to identify the analyst and personalise proposal screens.
