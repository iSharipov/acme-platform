package io.github.isharipov.acme.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class AcmePlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(AcmePlatformApplication.class, args);
    }
}
