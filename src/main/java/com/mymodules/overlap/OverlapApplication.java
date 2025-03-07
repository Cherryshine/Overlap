package com.mymodules.overlap;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;

@Log4j2
@SpringBootApplication
public class OverlapApplication {
    public static void main(String[] args) {
        SpringApplication.run(OverlapApplication.class, args);
    }

    @Bean
    public CommandLineRunner checkBeans(ApplicationContext ctx) {
        return args -> {
            log.info("📌 등록된 Bean 목록 확인...");
            String[] beanNames = ctx.getBeanDefinitionNames();
            Arrays.stream(beanNames)
                    .filter(name -> name.toLowerCase().contains("turnstile"))  // "turnstile" 포함된 Bean만 확인
                    .forEach(name -> log.info("✅ 등록된 Bean: " + name));
        };
    }


}