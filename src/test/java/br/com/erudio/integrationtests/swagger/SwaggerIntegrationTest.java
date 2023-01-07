package br.com.erudio.integrationtests.swagger;

import br.com.erudio.confing.TestConfigs;
import br.com.erudio.integrationtests.testcontainer.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Essa class de test É UM TESTE DE INTEGRAÇÃO QUE VAI VERIFICAR SE A PÁGINA DO SWAGGER ESTÁ REALMENTE SENDO GERADA, NO MOMENTO QUE A GENTE QUEBRAR
 * O SISTEMA E ESSA PÁGINA NÃO FOR MAIS GERADA, ESE TESTE VAI NOS AVISAR IMEDIATAMENTE QUE A GENTE QUEBROU OS NOSSOS TESTES;
 *
 * Como ele funciona?
 *Ele inicia um contexto, ele inicaliza uma imagem docker, e inicia um container do mysql, conecta-se a ele, aplica as migrations que a gente definiu, e aí sim ele executa os testes;
 */

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class SwaggerIntegrationTest extends AbstractIntegrationTest {

    @Test
    void shouldDisplaySwaggerUiPage() {
        var content =
            given()
                    .basePath("/swagger-ui/index.html")
                    .port(TestConfigs.SERVER_PORT)
                    .when()
                        .get()
                    .then()
                        .statusCode(200)
                    .extract()
                        .body()
                            .asString();

        assertTrue(content.contains("Swagger UI"));
    }

}
