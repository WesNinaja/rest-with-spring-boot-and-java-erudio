package br.com.erudio.service;

import br.com.erudio.controller.BookController;
import br.com.erudio.controller.PersonController;
import br.com.erudio.data.vo.v1.BookVO;
import br.com.erudio.data.vo.v1.PersonVO;
import br.com.erudio.exception.RequiredObjectIsNullException;
import br.com.erudio.exception.ResourceNotFoundException;
import br.com.erudio.mapper.DozerMapper;
import br.com.erudio.mapper.custom.PersonMapper;
import br.com.erudio.model.Book;
import br.com.erudio.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class BookService {

    private Logger logger = Logger.getLogger(BookService.class.getName());

    @Autowired
    BookRepository repository;

    @Autowired
    PersonMapper mapper;
    //Esse Assembler vai nos ajudar a criar links hateoas para nossas páginas
    @Autowired
    PagedResourcesAssembler<BookVO> assembler;

    public PagedModel<EntityModel<BookVO>> findAll(Pageable pageable) {
        logger.info("Finding all books");

        var bookPage = repository.findAll(pageable);

        //Convertendo a lista page para uma lista de VOs
        var bookVosPage = bookPage.map(p -> DozerMapper.parseObject(p, BookVO.class));

        //Links hateoas
        bookVosPage.map(p -> p.add(linkTo(methodOn(BookController.class).findById(p.getKey())).withSelfRel()));

        //Aqui estamos criando um link hateoas para o nosso objeto página
        Link link = linkTo(methodOn(BookController.class).findAll(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                "ASC")).withSelfRel();

        //Retornando a lista paginada
        return assembler.toModel(bookVosPage, link);
    }

    public BookVO findById(Long id) {
        logger.info("Finding one book!");
        Book entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID!"));
        BookVO bookVO = DozerMapper.parseObject(entity, BookVO.class);

        //Adicionando Heteoas
        bookVO.add(linkTo(methodOn(BookController.class).findById(id)).withSelfRel());
        return bookVO;
    }

    public BookVO create(BookVO book) {
        //Recebe um VO pra salvar uma entidade no banco
        if (book == null) throw new RequiredObjectIsNullException();
        logger.info("Creating one book!");
        Book entity = DozerMapper.parseObject(book, Book.class);
        BookVO vo = DozerMapper.parseObject(repository.save(entity), BookVO.class);
        //Adicionando Heteoas
        vo.add(linkTo(methodOn(BookController.class).findById(vo.getKey())).withSelfRel());
        return vo;
    }

    public BookVO update(BookVO book) {
        //Recebe um VO pra salvar uma entidade no banco
        if (book == null) throw new RequiredObjectIsNullException();

        logger.info("Updating one book!");
        Book entity = repository.findById(book.getKey())
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID!"));

        entity.setTitle(book.getTitle());
        entity.setPrice(book.getPrice());
        entity.setLaunchDate(book.getLaunchDate());
        entity.setAuthor(book.getAuthor());

        BookVO vo = DozerMapper.parseObject(repository.save(entity), BookVO.class);
        //Adicionando Heteoas
        vo.add(linkTo(methodOn(BookController.class).findById(vo.getKey())).withSelfRel());
        return vo;
    }

    public void delete(Long id) {
        logger.info("Deleting one book!");

        Book entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this ID!"));
        repository.delete(entity);
    }
}
