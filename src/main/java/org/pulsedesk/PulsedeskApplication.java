package org.pulsedesk;

import org.pulsedesk.ai.HuggingFaceRequest;
import org.pulsedesk.ai.HuggingFaceService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.List;

@EnableAsync
@SpringBootApplication
public class PulsedeskApplication {

    @Value("${ai.model}")
    private String aiModel;

    public static void main(String[] args) {
        SpringApplication.run(PulsedeskApplication.class, args);
    }


    @Bean
    CommandLineRunner runner(HuggingFaceService huggingFaceService) {
        return args -> {
            try {
                String prompt = "who is amigoscode";

                if (prompt == null || prompt.isBlank()) {
                    throw new IllegalArgumentException("Prompt must not be empty");
                }

                HuggingFaceRequest request = new HuggingFaceRequest(
                        aiModel,
                        List.of(new HuggingFaceRequest.Message("user", prompt.trim())),
                        false
                );

                var response = huggingFaceService.completion(request);
                System.out.println(response);

            } catch (Exception ex) {
                System.err.println("AI request failed: " + ex.getMessage());
            }
        };
    }
}