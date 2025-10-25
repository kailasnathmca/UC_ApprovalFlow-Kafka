# SAML 2.0 Demo

`SamlController` represents an endpoint secured with SAML 2.0 assertions.

## Flow
1. A partner organisation authenticates with its identity provider and receives a SAML assertion from Keycloak.
2. The assertion is sent to IPM when calling `/saml/info`.
3. The application would validate the assertion before giving access to proposal data.
