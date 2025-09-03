# Active Directory Federation Demo

`ActiveDirectoryFederationConfig` configures Keycloak to use Microsoft Active Directory as a user store for IPM.

## Flow
1. `createProvider()` prepares a component representation for the AD server.
2. The representation is sent to Keycloak's Admin API.
3. IPM users can then authenticate with their Windows domain credentials.
