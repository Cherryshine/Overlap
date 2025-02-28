package com.mymodules.overlap.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.client.WebClient;


@Configuration
public class WebClientConfig { // ✅ 클래스명 변경: WebClientConfig로 변경하여 명확하게 함.
    @Primary
    @Bean(name = "webClientBuilderBean")
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}