package br.com.erudio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RESTful API with Java 18 and Spring Boot 3")
                        .version("v1")
                        .description("Some description about your API")
                        .termsOfService("https://pub.erudio.com.br/meus-cursos")
                        .license(
                                new License()
                                        .name("Apache 2.0")
                                        .url("https://pub.erudio.com.br/meus-cursos")
                        )
                );
    }

}

    //BEAN - é um objeto que é instanciado, montado e gerenciado pelo spring IOC container, o spring IOC container busca as informações em xml, annotations ou em código sobre como os beans
    //devem ser instanciados, configurados e montados e sobre como eles se relacionam com outros beans
    //A resolução do relacionamento entre é definida como injeçãod e dependências.
    //Se vc cria uma class que depende de algum bean, a gente só precisa se preocupar com o que a nossa class depende, não com o que as nossas dependencias dependem.


