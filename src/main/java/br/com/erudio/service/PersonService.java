package br.com.erudio.service;

import br.com.erudio.controller.PersonController;
import br.com.erudio.data.vo.v1.PersonVO;
import br.com.erudio.data.vo.v2.PersonVOV2;
import br.com.erudio.exception.RequiredObjectIsNullException;
import br.com.erudio.exception.ResourceNotFoundException;
import br.com.erudio.mapper.DozerMapper;
import br.com.erudio.mapper.custom.PersonMapper;
import br.com.erudio.model.Person;
import br.com.erudio.repository.PersonRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class PersonService {

    private final Logger logger = Logger.getLogger(PersonService.class.getName());

    @Autowired
    PersonRepository repository;
    @Autowired
    PersonMapper mapper;
    //Esse Assembler vai nos ajudar a criar links hateoas para nossas páginas
    @Autowired
    PagedResourcesAssembler<PersonVO> assembler;

    public PagedModel<EntityModel<PersonVO>> findAll(Pageable pageable) {
        logger.info("Finding all people!");

        var personPage = repository.findAll(pageable);

        //Convertendo a lista page para uma lista de VOs
        var personVosPage = personPage.map(p -> DozerMapper.parseObject(p, PersonVO.class));

        //Links hateoas
        personVosPage.map(p -> p.add(linkTo(methodOn(PersonController.class).findById(p.getKey())).withSelfRel()));

        //Aqui estamos criando um link hateoas para o nosso objeto página
        Link link = linkTo(methodOn(PersonController.class).findAll(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                "ASC")).withSelfRel();

        //Retornando a lista paginada
        return assembler.toModel(personVosPage, link);
    }

    public PersonVO findById(Long id) {
        logger.info("Finding one person!");
        var entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID!"));

        PersonVO vo = DozerMapper.parseObject(entity, PersonVO.class);

        //Adicionando Heteoas
        vo.add(linkTo(methodOn(PersonController.class).findById(id)).withSelfRel());
        return vo;
    }

    public PersonVO create(PersonVO person) {
        if (person == null) throw new RequiredObjectIsNullException();
        logger.info("Creating one person!");
        var entity = DozerMapper.parseObject(person, Person.class);
        PersonVO vo = DozerMapper.parseObject(repository.save(entity), PersonVO.class);
        //Adicionando Heteoas
        vo.add(linkTo(methodOn(PersonController.class).findById(vo.getKey())).withSelfRel());
        return vo;
    }


    public PersonVOV2 createV2(PersonVOV2 person) {
        logger.info("Creating one person!");
        var entity = mapper.convertVoToEntity(person);
        PersonVOV2 personVOV2 = mapper.convertEntityToVo(repository.save(entity));
        return personVOV2;
    }


    public PersonVO update(PersonVO person) {
        if (person == null) throw new RequiredObjectIsNullException();

        logger.info("Updating one person!");

        Person entity = repository.findById(person.getKey())
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID!"));


        entity.setFirstName(person.getFirstName());
        entity.setLastName(person.getLastName());
        entity.setAddress(person.getAddress());
        entity.setGender(person.getGender());

        PersonVO vo = DozerMapper.parseObject(repository.save(entity), PersonVO.class);
        //Adicionando Heteoas
        vo.add(linkTo(methodOn(PersonController.class).findById(vo.getKey())).withSelfRel());
        return vo;
    }

    //Para esse método como eu estou fazendo uma escrita de dados, e o spring data não gerencia, preciso usar a annotation Transactional, visto que criamos nossa própria operação, e é de modificação, caso seja uma query de leitura não precisamos do @Transactional.
    @Transactional
    public PersonVO disablePerson(Long id) {
        logger.info("Disabling one person!");

        repository.disablePerson(id);

        var entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID!"));

        PersonVO vo = DozerMapper.parseObject(entity, PersonVO.class);

        //Adicionando Heteoas
        vo.add(linkTo(methodOn(PersonController.class).findById(id)).withSelfRel());
        return vo;
    }


    public void delete(Long id) {
        logger.info("Deleting one person!");

        Person entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID!"));
        logger.info("Updating one person!");

        repository.delete(entity);
    }
}
