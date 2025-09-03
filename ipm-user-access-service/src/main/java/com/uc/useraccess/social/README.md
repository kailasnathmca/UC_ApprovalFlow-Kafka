# Social Login Demo

`SocialLoginConfig` registers external identity providers such as GitHub for contractors working with IPM.

## Flow
1. `createProvider()` builds an `IdentityProviderRepresentation` for GitHub.
2. The representation is sent to Keycloak via the Admin API.
3. Contractors can authenticate to IPM using their social accounts to submit proposals.
