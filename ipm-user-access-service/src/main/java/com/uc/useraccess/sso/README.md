# SSO and SLO Demo

`SsoController` shows how IPM uses Keycloak for Single Sign-On and Single Sign-Out.

## Flow
1. User calls an IPM endpoint such as `/sso` and is redirected to Keycloak for login.
2. After successful authentication Keycloak establishes an SSO session allowing the user to navigate between IPM services without re-authenticating.
3. Invoking `/logout` would reach Keycloak's logout endpoint and terminate the session across all IPM modules.
