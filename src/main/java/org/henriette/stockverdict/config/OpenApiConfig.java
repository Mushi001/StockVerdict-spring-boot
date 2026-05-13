package org.henriette.stockverdict.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI stockVerdictOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("StockVerdict API")
                        .description("REST API documentation for the StockVerdict application.")
                        .version("v1.0.0")
                        .contact(new Contact().name("Henriette").email("support@stockverdict.com"))
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")));
    }
}
