package com.pm.taskapp.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI configuration for API documentation.
 */
@Configuration
@OpenAPIDefinition(info = @Info(title = "TaskApp Authentication API", description = "REST API for authentication and user management  and Project Management in TaskApp", version = "1.0.0", contact = @Contact(name = "TaskApp Team", email = "support@taskapp.com", url = "https://taskapp.com"), license = @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0"), termsOfService = "https://taskapp.com/terms"), servers = {
        @Server(url = "http://localhost:8080", description = "Local Development Server"),
        @Server(url = "https://api.taskapp.com", description = "Production Server")
})
@SecurityScheme(name = "bearerAuth", description = "JWT Bearer Token Authentication", scheme = "bearer", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", in = SecuritySchemeIn.HEADER)
public class SwaggerConfig {

    /**
     * Group for Authentication APIs.
     */
    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("authentication")
                .displayName("Authentication")
                .pathsToMatch("/api/auth/**")
                .build();
    }

    /**
     * Group for User Management APIs.
     */
    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("user-management")
                .displayName("User Management")
                .pathsToMatch("/api/users/**")
                .build();
    }

    /**
     * Group for Role Management APIs.
     */
    @Bean
    public GroupedOpenApi roleApi() {
        return GroupedOpenApi.builder()
                .group("role-management")
                .displayName("Role Management")
                .pathsToMatch("/api/roles/**")
                .build();
    }

    /**
     * Group for Permission Management APIs.
     */
    @Bean
    public GroupedOpenApi permissionApi() {
        return GroupedOpenApi.builder()
                .group("permission-management")
                .displayName("Permission Management")
                .pathsToMatch("/api/permissions/**")
                .build();
    }

    @Bean
    public GroupedOpenApi projectApi() {
        return GroupedOpenApi.builder()
                .group("project-management")
                .displayName("Project Management")
                .pathsToMatch("/api/projects/**")
                .build();
    }

    @Bean
    public GroupedOpenApi projectMemberApi() {
        return GroupedOpenApi.builder()
                .group("project-members")
                .displayName("Project Members")
                .pathsToMatch("/api/projects/*/members/**")
                .build();
    }

    /**
     * Group for all APIs.
     */
    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("all")
                .displayName("All APIs")
                .pathsToMatch("/api/**")
                .build();
    }
}