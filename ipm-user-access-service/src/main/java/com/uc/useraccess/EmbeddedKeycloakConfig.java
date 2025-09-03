package com.uc.useraccess;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Boots an embedded Keycloak instance for demo purposes.
 * <p>
 * The embedded server is represented by a Keycloak admin client that
 * interacts with a Keycloak server running in the same JVM. In a real
 * project this would start the Keycloak distribution programmatically but
 * for documentation purposes the admin client is sufficient to demonstrate
 * configuration points.
 */
@Configuration
public class EmbeddedKeycloakConfig {

    private Keycloak keycloak;

    /**
     * Starts the embedded Keycloak server. Here we simply create an admin
     * client but the pattern mirrors what would happen when bootstrapping
     * the server via Keycloak's Quarkus distribution.
     */
    @PostConstruct
    public void start() {
        keycloak = KeycloakBuilder.builder()
                .serverUrl("http://localhost:8080")
                .realm("master")
                .clientId("admin-cli")
                .grantType(OAuth2Constants.PASSWORD)
                .username("admin")
                .password("admin")
                .build();
    }

    /**
     * Releases the admin client on shutdown.
     */
    @PreDestroy
    public void stop() {
        if (keycloak != null) {
            keycloak.close();
        }
    }

    /**
     * Makes the admin client available to other components that need to
     * interact with the embedded Keycloak instance.
     */
    @Bean
    public Keycloak keycloakAdminClient() {
        return keycloak;
    }
}
