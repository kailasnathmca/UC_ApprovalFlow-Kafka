package com.uc.useraccess.rbac;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple Role-Based Access Control (RBAC) example. Access to the admin
 * endpoint is restricted to users having the {@code admin} role which is
 * mapped from Keycloak roles via the Spring Security adapter.
 */
@RestController
public class RbacController {

    @GetMapping("/admin")
    @PreAuthorize("hasRole('admin')")
    public String admin() {
        // Only users with the "admin" role can invoke this method.
        return "RBAC protected admin resource";
    }
}
