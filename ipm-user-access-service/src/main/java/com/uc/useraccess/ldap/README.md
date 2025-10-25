# LDAP Federation Demo

`LdapFederationConfig` shows how to register a corporate LDAP directory with Keycloak so employees can log into IPM using existing accounts.

## Flow
1. `createProvider()` builds a `ComponentRepresentation` for the LDAP server.
2. The configuration is posted to the Keycloak Admin API.
3. After registration, IPM delegates user authentication to the LDAP store.
