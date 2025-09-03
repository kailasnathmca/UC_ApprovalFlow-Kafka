package com.uc.useraccess;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the user access service.
 * <p>
 * The service hosts an embedded Keycloak server and exposes a set of
 * demonstration endpoints showing various authentication and federation
 * features supported by Keycloak in the context of the Investment Proposal
 * Management system.
 */
@SpringBootApplication
public class IpmUserAccessServiceApplication {

    /**
     * Boots the Spring application which in turn launches the embedded
     * Keycloak server. The examples in this module are intentionally simple
     * and heavily commented to serve as a reference for integrating
     * Keycloak with different identity protocols used throughout IPM.
     *
     * @param args ignored application arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(IpmUserAccessServiceApplication.class, args);
    }
}
