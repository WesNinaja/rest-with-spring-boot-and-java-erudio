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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

@Service
public class PersonService {

    private final Logger logger = Logger.getLogger(PersonService.class.getName());

    @Autowired
    PersonRepository repository;

    @Autowired
    PersonMapper mapper;

    public List<PersonVO> findAll() {
        logger.info("Finding all people!");
        List<PersonVO> personVOS = DozerMapper.parseListObjects(repository.findAll(), PersonVO.class);
        personVOS
                .stream()
                .forEach(p -> p.add(linkTo(methodOn(PersonController.class).findById(p.getKey())).withSelfRel()));
        return personVOS;
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

    public void delete(Long id) {
        logger.info("Deleting one person!");

        Person entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID!"));
        logger.info("Updating one person!");

        repository.delete(entity);
    }
}
