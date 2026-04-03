package org.pulsedesk.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.pulsedesk.ai.HuggingFaceService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;

@Configuration
@ImportHttpServices(group = "huggingFace", types = HuggingFaceService.class)
public class HttpClientConfig {

    @Value("${ai.huggingface.token}")
    private String huggingFaceToken;

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    RestClientHttpServiceGroupConfigurer groupConfigurer() {
        return groups -> {
            groups
                    .filterByName("huggingFace")
                    .forEachClient((g, builder) -> {
                        builder.baseUrl("https://router.huggingface.co/v1")
                                .defaultHeader("Authorization", "Bearer " + huggingFaceToken)
                                .build();
                    });
        };
    }
}
