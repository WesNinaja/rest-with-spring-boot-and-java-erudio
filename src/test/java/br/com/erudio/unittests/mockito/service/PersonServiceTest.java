package br.com.erudio.unittests.mockito.service;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

import br.com.erudio.data.vo.v1.PersonVO;
import br.com.erudio.exception.RequiredObjectIsNullException;
import br.com.erudio.model.Person;
import br.com.erudio.repository.PersonRepository;
import br.com.erudio.service.PersonService;
import br.com.erudio.unittests.mapper.mocks.MockPerson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class PersonServiceTest {

    MockPerson input;

    @InjectMocks
    private PersonService service;

    @Mock
    private PersonRepository personRepository;

    @BeforeEach
    void setUpMocks() {
        input = new MockPerson();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindById() {
        Person entity = input.mockEntity(1);
        entity.setId(1L);
        //Quando o método repository for chamado a gente vai retornar um mock, é isso que vamos implementar com esse mockito
        when(personRepository.findById(1L)).thenReturn(Optional.of(entity));

        //Agora vamos efetivamente chamar o service com o metodo findById
        var result = service.findById(1L);

        assertNotNull(result);
        assertNotNull(result.getKey());
        assertNotNull(result.getLinks());
        System.out.println(result.toString());
        assertTrue(result.toString().contains("links: [</api/person/v1/1>;rel=\"self\"]"));
        assertEquals("Addres Test1", result.getAddress());
        assertEquals("First Name Test1", result.getFirstName());
        assertEquals("Last Name Test1", result.getLastName());
        assertEquals("Female", result.getGender());

    }


//    @Test
//    void findAll() {
//        List<Person> list = input.mockEntityList();
//        //Quando o método repository for chamado a gente vai retornar um mock, é isso que vamos implementar com esse mockito
//        when(personRepository.findAll()).thenReturn(list);
//
//        //Agora vamos efetivamente chamar o service com o metodo findById
//        var people = service.findAll();
//
//        assertNotNull(people);
//        assertEquals(14, people.size());
//
//        var personOne = people.get(1);
//
//        assertNotNull(personOne);
//        assertNotNull(personOne.getKey());
//        assertNotNull(personOne.getLinks());
//        System.out.println(personOne.toString());
//        assertTrue(personOne.toString().contains("links: [</api/person/v1/1>;rel=\"self\"]"));
//        assertEquals("Addres Test1", personOne.getAddress());
//        assertEquals("First Name Test1", personOne.getFirstName());
//        assertEquals("Last Name Test1", personOne.getLastName());
//        assertEquals("Female", personOne.getGender());
//
//        var personFour = people.get(4);
//
//        assertNotNull(personFour);
//        assertNotNull(personFour.getKey());
//        assertNotNull(personFour.getLinks());
//        System.out.println(personFour.toString());
//        assertTrue(personFour.toString().contains("links: [</api/person/v1/4>;rel=\"self\"]"));
//        assertEquals("Addres Test4", personFour.getAddress());
//        assertEquals("First Name Test4", personFour.getFirstName());
//        assertEquals("Last Name Test4", personFour.getLastName());
//        assertEquals("Male", personFour.getGender());
//
//        var personSeven = people.get(7);
//
//        assertNotNull(personSeven);
//        assertNotNull(personSeven.getKey());
//        assertNotNull(personSeven.getLinks());
//        System.out.println(personSeven.toString());
//        assertTrue(personSeven.toString().contains("links: [</api/person/v1/7>;rel=\"self\"]"));
//        assertEquals("Addres Test7", personSeven.getAddress());
//        assertEquals("First Name Test7", personSeven.getFirstName());
//        assertEquals("Last Name Test7", personSeven.getLastName());
//        assertEquals("Female", personSeven.getGender());
//    }

    @Test
    void testCreate() {
        //Antes de ser persistida
        Person entity = input.mockEntity(1);
        //Depois de ser persistida
        Person persisted = entity;
        persisted.setId(1L);

        PersonVO vo = input.mockVO(1);
        vo.setKey(1L);

        //Quando o método repository for chamado a gente vai retornar um mock, é isso que vamos implementar com esse mockito
        when(personRepository.save(entity)).thenReturn(persisted);

        var result = service.create(vo);

        assertNotNull(result);
        assertNotNull(result.getKey());
        assertNotNull(result.getLinks());
        assertTrue(result.toString().contains("links: [</api/person/v1/1>;rel=\"self\"]"));
        assertEquals("Addres Test1", result.getAddress());
        assertEquals("First Name Test1", result.getFirstName());
        assertEquals("Last Name Test1", result.getLastName());
        assertEquals("Female", result.getGender());
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
        Person entity = input.mockEntity(1);
        entity.setId(1L);
        //Depois de ser persistida
        Person persisted = entity;
        persisted.setId(1L);

        PersonVO vo = input.mockVO(1);
        vo.setKey(1L);

        //Quando o método repository for chamado a gente vai retornar um mock, é isso que vamos implementar com esse mockito
        when(personRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(personRepository.save(entity)).thenReturn(persisted);

        var result = service.update(vo);

        assertNotNull(result);
        assertNotNull(result.getKey());
        assertNotNull(result.getLinks());
        assertTrue(result.toString().contains("links: [</api/person/v1/1>;rel=\"self\"]"));
        assertEquals("Addres Test1", result.getAddress());
        assertEquals("First Name Test1", result.getFirstName());
        assertEquals("Last Name Test1", result.getLastName());
        assertEquals("Female", result.getGender());
    }

    @Test
    void testDelete() {
        Person entity = input.mockEntity(1);
        entity.setId(1L);
        //Quando o método repository for chamado a gente vai retornar um mock, é isso que vamos implementar com esse mockito
        when(personRepository.findById(1L)).thenReturn(Optional.of(entity));

        //Agora vamos efetivamente chamar o service com o metodo findById
        service.delete(1L);
    }
}