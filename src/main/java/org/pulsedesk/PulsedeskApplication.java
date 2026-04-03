package org.pulsedesk;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class PulsedeskApplication {

    @Value("${ai.model}")
    private String aiModel;

    public static void main(String[] args) {
        SpringApplication.run(PulsedeskApplication.class, args);
    }


}