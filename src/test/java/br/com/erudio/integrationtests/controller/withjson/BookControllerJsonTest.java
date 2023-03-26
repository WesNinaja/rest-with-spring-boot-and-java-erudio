package br.com.erudio.integrationtests.controller.withjson;

import br.com.erudio.confing.TestConfigs;
import br.com.erudio.integrationtests.testcontainer.AbstractIntegrationTest;
import br.com.erudio.integrationtests.vo.AccountCredentialsVO;
import br.com.erudio.integrationtests.vo.BookVO;
import br.com.erudio.integrationtests.vo.TokenVO;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.core.type.TypeReference;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.DeserializationFeature;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.JsonMappingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BookControllerJsonTest extends AbstractIntegrationTest {

    private static RequestSpecification specification;
    private static ObjectMapper objectMapper;
    private static BookVO book;

    @BeforeAll
    public static void setup() {
        objectMapper = new ObjectMapper();
        //Quando recebermos o json, ele vai ter campos de hateoas preenchidos, e o nosso VO não reconhece esses atributos do hateoas como links, e por isso desabilitamos falhas em propiedades desconhecidas
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        book = new BookVO();
    }

    @Test
    @Order(0)
    void authorization() throws JsonMappingException, JsonProcessingException {
        AccountCredentialsVO user = new AccountCredentialsVO("leandro", "admin123");

        var accessToken = given()
                .basePath("/auth/signin")
                .port(TestConfigs.SERVER_PORT)
                .contentType(TestConfigs.CONTENT_TYPE_JSON)
                .body(user)
                .when()
                .post()
                .then()
                .statusCode(200)
                //Nesse caso, usamos o asString porque senão ele vai usar o objectMapper do restAssured e vai ter problemas com as serializationFeatures pra propriedades desconhecidas
                .extract()
                .body()
                .as(TokenVO.class)
                .getAccessToken();

        specification = new RequestSpecBuilder()
                .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + accessToken)
                .setBasePath("/api/book/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();

    }

    @Test
    @Order(1)
    void testCreate() throws IOException {
        mockBook();

        var content = given().spec(specification)
                .contentType(TestConfigs.CONTENT_TYPE_JSON)
                .body(book)
                .when()
                .post()
                .then()
                .statusCode(200)
                //Nesse caso, usamos o asString porque senão ele vai usar o objectMapper do restAssured e vai ter problemas com as serializationFeatures pra propriedades desconhecidas
                .extract()
                .body()
                .asString();

        BookVO persistedBook = objectMapper.readValue(content, BookVO.class);
        book = persistedBook;

        assertNotNull(persistedBook);
        assertNotNull(persistedBook.getId());

        assertEquals("Some Author", persistedBook.getAuthor());
        assertEquals("Some Title", persistedBook.getTitle());
        assertEquals(25D, persistedBook.getPrice());
        assertNotNull(persistedBook.getLaunchDate());
    }

    @Test
    @Order(2)
    void testUpdate() throws IOException {
        book.setAuthor("Piquet Souto");

        var content = given().spec(specification)
                .contentType(TestConfigs.CONTENT_TYPE_JSON)
                .body(book)
                .when()
                .post()
                .then()
                .statusCode(200)
                //Nesse caso, usamos o asString porque senão ele vai usar o objectMapper do restAssured e vai ter problemas com as serializationFeatures pra propriedades desconhecidas
                .extract()
                .body()
                .asString();

        BookVO persistedBook = objectMapper.readValue(content, BookVO.class);
        book = persistedBook;

        assertNotNull(persistedBook);
        assertNotNull(persistedBook.getId());

        assertEquals("Piquet Souto", persistedBook.getAuthor());
        assertEquals("Some Title", persistedBook.getTitle());
        assertEquals(25D, persistedBook.getPrice());
        assertNotNull(persistedBook.getLaunchDate());
    }

    @Test
    @Order(3)
    void testFindById() throws IOException {
        mockBook();

        var content = given().spec(specification)
                .contentType(TestConfigs.CONTENT_TYPE_JSON)
                .header(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_ERUDIO)
                .pathParams("id", book.getId())
                .when()
                .get("{id}")
                .then()
                .statusCode(200)
                //Nesse caso, usamos o asString porque senão ele vai usar o objectMapper do restAssured e vai ter problemas com as serializationFeatures pra propriedades desconhecidas
                .extract()
                .body()
                .asString();

        BookVO persistedBook = objectMapper.readValue(content, BookVO.class);
        book = persistedBook;

        assertNotNull(persistedBook);
        assertNotNull(persistedBook.getId());

        assertEquals("Piquet Souto", persistedBook.getAuthor());
        assertEquals("Some Title", persistedBook.getTitle());
        assertEquals(25D, persistedBook.getPrice());
        assertNotNull(persistedBook.getLaunchDate());
    }

    @Test
    @Order(4)
    void testDelete() throws IOException {

        given().spec(specification)
                .contentType(TestConfigs.CONTENT_TYPE_JSON)
                .pathParams("id", book.getId())
                .when()
                .delete("{id}")
                .then()
                .statusCode(204);
    }

    @Test
    @Order(5)
    void testFindAll() throws IOException {

        var content = given().spec(specification)
                .contentType(TestConfigs.CONTENT_TYPE_JSON)
                .when()
                .get()
                .then()
                .statusCode(200)
                //Nesse caso, usamos o asString porque senão ele vai usar o objectMapper do restAssured e vai ter problemas com as serializationFeatures pra propriedades desconhecidas
                .extract()
                .body()
                .asString();

        List<BookVO> booksVO = objectMapper.readValue(content, new TypeReference<List<BookVO>>() {});

        BookVO foundBookOne = booksVO.get(0);

        assertNotNull(foundBookOne);
        assertNotNull(foundBookOne.getId());

        assertEquals("Michael C. Feathers", foundBookOne.getAuthor());
        assertEquals("Working effectively with legacy code", foundBookOne.getTitle());
        assertEquals(49.00, foundBookOne.getPrice());
        assertNotNull(foundBookOne.getLaunchDate());

        BookVO foundBookSix = booksVO.get(4);

        assertNotNull(foundBookSix);
        assertNotNull(foundBookSix.getId());

        assertEquals("Steve McConnell", foundBookSix.getAuthor());
        assertEquals("Code complete", foundBookSix.getTitle());
        assertEquals(58.00, foundBookSix.getPrice());
        assertNotNull(foundBookSix.getLaunchDate());
    }

    @Test
    @Order(6)
    void testFindAllWithoutLogin() throws IOException {

        RequestSpecification specificationWithoutToken= new RequestSpecBuilder()
                .setBasePath("/api/book/v1")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();


        given().spec(specificationWithoutToken)
                .contentType(TestConfigs.CONTENT_TYPE_JSON)
                .when()
                .get()
                .then()
                .statusCode(403);
    }

    public void mockBook() {
        book.setAuthor("Some Author");
        book.setLaunchDate(new Date());
        book.setPrice(25D);
        book.setTitle("Some Title");
    }

}