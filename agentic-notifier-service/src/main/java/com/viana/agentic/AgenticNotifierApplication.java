package com.viana.agentic;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AgenticNotifierApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgenticNotifierApplication.class, args);
    }

    @Bean
    CommandLineRunner printProps(
            @Value("${agentic.email.from:NOT_FOUND}") String from,
            @Value("${agentic.email.to:NOT_FOUND}") String to
    ) {
        return args -> {
            System.out.println("agentic.email.from=" + from);
            System.out.println("agentic.email.to=" + to);
        };
    }
}
