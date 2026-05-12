package com.karma.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class KarmaPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(KarmaPlatformApplication.class, args);
    }
}
