# OAuth 2.0 Demo

`OAuth2Controller` exposes an endpoint protected by OAuth2 bearer tokens.

## Flow
1. A client obtains an access token from Keycloak.
2. The token is presented in the `Authorization` header when calling `/oauth2/protected`.
3. Only requests with a valid token succeed, mimicking protection of IPM proposal APIs.
