package software.ulpgc.es.monolith.app.controllers;

import org.springframework.web.bind.annotation.*;
import software.ulpgc.es.monolith.app.Main;
import software.ulpgc.es.monolith.domain.control.CommandFactory;
import software.ulpgc.es.monolith.domain.control.books.*;
import software.ulpgc.es.monolith.domain.model.Book;
import software.ulpgc.es.monolith.domain.model.ISBN;

import java.util.List;

@RestController
@RequestMapping("/books")
public class BookController {
    private final CommandFactory commandFactory;

    public BookController(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    @GetMapping
    public List<Book> getAllBooks() {
        final List<Book>[] resultHolder = new List[1];
        GetAllBooksCommand.Output output = books -> resultHolder[0] = books;
        this.commandFactory.with(Main.NoInput.INSTANCE, output).build("getBooks").execute();
        return resultHolder[0];
    }

    @GetMapping("/{isbn}")
    public Book getBook(@PathVariable String isbn) {
        final Book[] resultHolder = new Book[1];
        GetBookCommand.Input input = () -> new ISBN(isbn);
        GetBookCommand.Output output = result -> resultHolder[0] = result;
        this.commandFactory.with(input, output).build("getBook").execute();
        return resultHolder[0];
    }

    @PostMapping
    public Book addBook(@RequestBody Book book) {
        final Book[] resultHolder = new Book[1];
        CreateBookCommand.Input input = () -> book;
        CreateBookCommand.Output output = result -> resultHolder[0] = result;
        this.commandFactory.with(input, output).build("createBook").execute();
        return resultHolder[0];
    }

    @PutMapping("/{isbn}")
    public Book updateBook(@PathVariable String isbn, @RequestBody Book book) {
        final Book[] resultHolder = new Book[1];
        UpdateBookCommand.Input input = () -> book;
        UpdateBookCommand.Output output = result -> resultHolder[0] = result;
        this.commandFactory.with(input, output).build("updateBook").execute();
        return resultHolder[0];
    }

    @DeleteMapping("/{isbn}")
    public Book deleteBook(@PathVariable String isbn) {
        final Book[] resultHolder = new Book[1];
        DeleteBookCommand.Input input = () -> new ISBN(isbn);
        DeleteBookCommand.Output output = result -> resultHolder[0] = result;
        this.commandFactory.with(input, output).build("deleteBook").execute();
        return resultHolder[0];
    }
}
