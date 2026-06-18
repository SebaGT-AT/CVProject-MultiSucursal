package com.multisucursal.inventory.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.bootstrap.admin")
public class BootstrapAdminProperties {

    private String name;
    private String email;
    private String password;
}

