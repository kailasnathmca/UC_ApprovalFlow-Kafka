# Role-Based Access Control Demo

`RbacController` secures endpoints based on Keycloak roles.

## Flow
1. A user with the `admin` role calls `/admin`.
2. Spring Security's `@PreAuthorize` checks the role from the Keycloak token.
3. Only users with the role can perform privileged IPM actions like approving proposals.
