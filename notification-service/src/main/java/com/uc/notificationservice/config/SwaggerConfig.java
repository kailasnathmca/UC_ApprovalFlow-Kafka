package com.uc.notificationservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Swagger UI at /swagger-ui */
@Configuration
public class SwaggerConfig {
    @Bean public OpenAPI api(){ return new OpenAPI().info(new Info().title("notification-service API").version("v1")); }
}
