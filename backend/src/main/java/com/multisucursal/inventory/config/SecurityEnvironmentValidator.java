package com.multisucursal.inventory.config;

import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@RequiredArgsConstructor
public class SecurityEnvironmentValidator {

    public static final String DEFAULT_JWT_SECRET =
        "bXVsdGlzdWN1cnNhbC1kZXZlbG9wbWVudC1zZWNyZXQta2V5LTIwMjYtY2hhbmdlLW1l";

    public static final String DEFAULT_BOOTSTRAP_ADMIN_PASSWORD = "Admin12345!";

    private final Environment environment;
    private final AppSecurityProperties appSecurityProperties;
    private final BootstrapAdminProperties bootstrapAdminProperties;

    @Bean
    public ApplicationRunner validateSecurityConfiguration() {
        return args -> {
            if (!Arrays.asList(environment.getActiveProfiles()).contains("prod")) {
                return;
            }

            if (DEFAULT_JWT_SECRET.equals(appSecurityProperties.getJwt().getSecret())) {
                throw new IllegalStateException("JWT secret must be overridden in production");
            }

            if (DEFAULT_BOOTSTRAP_ADMIN_PASSWORD.equals(bootstrapAdminProperties.getPassword())) {
                throw new IllegalStateException("Bootstrap admin password must be overridden in production");
            }
        };
    }
}
