package com.college.placement_backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig{
    // If multiple students load the swagger documentation, instead of multiple new objects being created, 
    // it creates one and points all of the students to it
    @Bean
    public OpenAPI customOpenAPI(){
        return new OpenAPI()
            .info(new Info()
                    .title("College Placement Management System API")
                    .version("1.0")
                    .description("Enterprise REST API for managing student applications, corporate recruiters and job listings.")
                    .contact(new Contact().name("Vivek").email("waldia.vivek@gmail.com"))
                )
            .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
            .components(new Components().addSecuritySchemes("BearerAuth", new SecurityScheme()
                            .name("BearerAuth")
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                        ));
    }
}