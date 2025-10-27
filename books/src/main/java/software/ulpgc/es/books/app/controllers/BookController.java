package software.ulpgc.es.books.app.controllers;

import org.springframework.web.bind.annotation.*;
import software.ulpgc.es.books.domain.model.Book;
import software.ulpgc.es.books.domain.repository.BookRepository;

import java.util.List;

@RestController
@RequestMapping("/books")
public class BookController {
    private final BookRepository repository;

    public BookController(BookRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Book> getAllBooks() {
        return repository.getAllBooks();
    }

    @GetMapping("/{isbn}")
    public Book getBook(@PathVariable String isbn) {
        return repository.getBook(isbn);
    }

    @PostMapping
    public Book addBook(@RequestBody Book book) {
        boolean saved = repository.saveBook(book);
        if (!saved) throw new RuntimeException("The book could not be saved");
        return book;
    }

    @PutMapping("/{isbn}")
    public Book updateBook(@PathVariable String isbn, @RequestBody Book book) {
        return repository.updateBook(new Book(isbn, book.title(), book.author(), book.publisher()));
    }

    @DeleteMapping("/{isbn}")
    public Book deleteBook(@PathVariable String isbn) {
        return repository.deleteBook(isbn);
    }
}
