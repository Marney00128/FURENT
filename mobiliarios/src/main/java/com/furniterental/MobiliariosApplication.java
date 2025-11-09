package com.furniterental;

import com.furniterental.config.MailProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(MailProperties.class)
public class MobiliariosApplication {

    public static void main(String[] args) {
        SpringApplication.run(MobiliariosApplication.class, args);
    }
}
