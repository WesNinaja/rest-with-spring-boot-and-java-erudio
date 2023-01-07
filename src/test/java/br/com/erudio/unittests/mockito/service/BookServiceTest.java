package br.com.erudio.unittests.mockito.service;

import br.com.erudio.data.vo.v1.BookVO;
import br.com.erudio.exception.RequiredObjectIsNullException;
import br.com.erudio.model.Book;
import br.com.erudio.repository.BookRepository;
import br.com.erudio.service.BookService;
import br.com.erudio.unittests.mapper.mocks.MockBook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    MockBook input;

    @InjectMocks
    BookService service;

    @Mock
    BookRepository repository;

    @BeforeEach
    void setUp() {
        input = new MockBook();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindAll() {
        List<Book> list = input.mockEntityList();
        //Quando o método repository for chamado a gente vai retornar um mock, é isso que vamos implementar com esse mockito
        when(repository.findAll()).thenReturn(list);

        //Agora vamos efetivamente chamar o service com o metodo findById
        var books = service.findAll();

        assertNotNull(books);
        assertEquals(14, books.size());

        var oneBook = books.get(1);

        assertNotNull(oneBook);
        assertNotNull(oneBook.getKey());
        assertNotNull(oneBook.getLinks());
        System.out.println(oneBook.toString());
        assertTrue(oneBook.toString().contains("links: [</api/book/v1/1>;rel=\"self\"]"));
        assertEquals(25D, oneBook.getPrice());
        assertEquals("Some Title1", oneBook.getTitle());

        var twoBook = books.get(2);

        assertNotNull(twoBook);
        assertNotNull(twoBook.getKey());
        assertNotNull(twoBook.getLinks());
        assertTrue(twoBook.toString().contains("links: [</api/book/v1/2>;rel=\"self\"]"));
        assertEquals(25D, twoBook.getPrice());
        assertEquals("Some Title2", twoBook.getTitle());

        var sevenBook = books.get(7);

        assertNotNull(sevenBook);
        assertNotNull(sevenBook.getKey());
        assertNotNull(sevenBook.getLinks());
        assertTrue(sevenBook.toString().contains("links: [</api/book/v1/7>;rel=\"self\"]"));
        assertEquals(25D, sevenBook.getPrice());
        assertEquals("Some Title7", sevenBook.getTitle());

    }

    @Test
    void testFindById() {
        Book entity = input.mockEntity(1);

        //Quando o método repository for chamado a gente vai retornar um mock, é isso que vamos implementar com esse mockito
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        //Agora vamos efetivamente chamar o service com o metodo findById
        var result = service.findById(1L);

        assertNotNull(result);
        assertNotNull(result.getKey());
        assertNotNull(result.getLinks());
        assertTrue(result.toString().contains("links: [</api/book/v1/1>;rel=\"self\"]"));
        assertEquals(25D, result.getPrice());
        assertEquals("Some Title1", result.getTitle());
    }

    @Test
    void testCreate() {
        Book entity = input.mockEntity(1);
        entity.setId(1L);

        Book persisted = entity;
        persisted.setId(1L);

        BookVO vo = input.mockVO(1);
        vo.setKey(1L);

        when(repository.save(entity)).thenReturn(persisted);

        var result = service.create(vo);

        assertNotNull(result);
        assertNotNull(result.getKey());
        assertNotNull(result.getLinks());

        assertTrue(result.toString().contains("links: [</api/book/v1/1>;rel=\"self\"]"));
        assertEquals("Some Author1", result.getAuthor());
        assertEquals("Some Title1", result.getTitle());
        assertEquals(25D, result.getPrice());
        assertNotNull(result.getLaunchDate());
    }

    @Test
    void testCreateWithNullPerson() {
        Exception exception = assertThrows(RequiredObjectIsNullException.class, () -> {
            service.create(null);
        });

        String expectedMessage = "It is not allowed to persist a null object!";
        String actualMessge = exception.getMessage();
        assertTrue(actualMessge.contains(expectedMessage));

    }

    @Test
    void testUpdateWithNullPerson() {
        Exception exception = assertThrows(RequiredObjectIsNullException.class, () -> {
            service.update(null);
        });

        String expectedMessage = "It is not allowed to persist a null object!";
        String actualMessge = exception.getMessage();
        assertTrue(actualMessge.contains(expectedMessage));

    }

    @Test
    void testUpdate() {
        //Antes de ser persistida
        Book entity = input.mockEntity(1);

        Book persisted = entity;
        persisted.setId(1L);

        BookVO vo = input.mockVO(1);
        vo.setKey(1L);

        //Quando o método repository for chamado a gente vai retornar um mock, é isso que vamos implementar com esse mockito
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(persisted);

        //Agora vamos efetivamente chamar o service com o metodo findById
        var result = service.update(vo);

        assertNotNull(result);
        assertNotNull(result.getKey());
        assertNotNull(result.getLinks());
        assertTrue(result.toString().contains("links: [</api/book/v1/1>;rel=\"self\"]"));
        assertEquals(25D, result.getPrice());
        assertEquals("Some Title1", result.getTitle());
    }

    @Test
    void testDelete() {
        Book entity = input.mockEntity(1);
        entity.setId(1L);

        //Quando o método repository for chamado a gente vai retornar um mock, é isso que vamos implementar com esse mockito
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        //Agora vamos efetivamente chamar o service com o metodo findById
        service.delete(1L);
    }
}