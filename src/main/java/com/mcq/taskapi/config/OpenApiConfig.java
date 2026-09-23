package com.mcq.taskapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI taskApiOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Task Management API")
                        .description("A portfolio demo Spring Boot CRUD REST API for managing tasks")
                        .version("v1")
                        .contact(new Contact().name("Richard McQuiston")));
    }
}
