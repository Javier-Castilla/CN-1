package software.ulpgc.es.monolith.app.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.ulpgc.es.monolith.app.Main;
import software.ulpgc.es.monolith.domain.control.CommandFactory;
import software.ulpgc.es.monolith.domain.control.books.*;
import software.ulpgc.es.monolith.domain.io.repository.exceptions.books.BookNotFoundException;
import software.ulpgc.es.monolith.domain.io.repository.exceptions.books.BooksDatabaseException;
import software.ulpgc.es.monolith.domain.io.repository.exceptions.books.DuplicateBookException;
import software.ulpgc.es.monolith.domain.model.Book;
import software.ulpgc.es.monolith.domain.model.ISBN;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/books")
public class BookController {
    private final CommandFactory commandFactory;

    public BookController(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }


    @GetMapping
    public ResponseEntity<?> getAllBooks() {
        final List<Book>[] resultHolder = new List[1];
        try {
            GetAllBooksCommand.Output output = books -> resultHolder[0] = books;
            this.commandFactory.with(Main.NoInput.INSTANCE, output).build("getBooks").execute();
            return ResponseEntity.ok(resultHolder[0]);
        } catch (BooksDatabaseException e) {
            return buildError(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (Exception e) {
            return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error retrieving books.");
        }
    }

    @GetMapping("/{isbn}")
    public ResponseEntity<?> getBook(@PathVariable String isbn) {
        final Book[] resultHolder = new Book[1];
        try {
            GetBookCommand.Input input = () -> new ISBN(isbn);
            GetBookCommand.Output output = result -> resultHolder[0] = result;
            this.commandFactory.with(input, output).build("getBook").execute();

            return Optional.ofNullable(resultHolder[0])
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> buildError(HttpStatus.NOT_FOUND, "Book not found."));
        } catch (BookNotFoundException e) {
            return buildError(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            return buildError(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (BooksDatabaseException e) {
            return buildError(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (Exception e) {
            return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error retrieving book.");
        }
    }

    @PostMapping
    public ResponseEntity<?> addBook(@RequestBody Book book) {
        final Book[] resultHolder = new Book[1];
        try {
            CreateBookCommand.Input input = () -> book;
            CreateBookCommand.Output output = result -> resultHolder[0] = result;
            this.commandFactory.with(input, output).build("createBook").execute();

            return ResponseEntity.status(HttpStatus.CREATED).body(resultHolder[0]);
        } catch (DuplicateBookException e) {
            return buildError(HttpStatus.CONFLICT, e.getMessage());
        } catch (IllegalArgumentException e) {
            return buildError(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (BooksDatabaseException e) {
            return buildError(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (Exception e) {
            return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error creating book.");
        }
    }

    @PutMapping("/{isbn}")
    public ResponseEntity<?> updateBook(@PathVariable String isbn, @RequestBody Book book) {
        final Book[] resultHolder = new Book[1];
        try {
            UpdateBookCommand.Input input = () -> book;
            UpdateBookCommand.Output output = result -> resultHolder[0] = result;
            this.commandFactory.with(input, output).build("updateBook").execute();

            return Optional.ofNullable(resultHolder[0])
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> buildError(HttpStatus.NOT_FOUND, "Book not found for update."));
        } catch (BookNotFoundException e) {
            return buildError(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            return buildError(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (BooksDatabaseException e) {
            return buildError(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (Exception e) {
            return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error updating book.");
        }
    }

    @DeleteMapping("/{isbn}")
    public ResponseEntity<?> deleteBook(@PathVariable String isbn) {
        final Book[] resultHolder = new Book[1];
        try {
            DeleteBookCommand.Input input = () -> new ISBN(isbn);
            DeleteBookCommand.Output output = result -> resultHolder[0] = result;
            this.commandFactory.with(input, output).build("deleteBook").execute();

            return Optional.ofNullable(resultHolder[0])
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> buildError(HttpStatus.NOT_FOUND, "Book not found for deletion."));
        } catch (BookNotFoundException e) {
            return buildError(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            return buildError(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (BooksDatabaseException e) {
            return buildError(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (Exception e) {
            return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error deleting book.");
        }
    }

    private <T> ResponseEntity<T> buildError(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body((T) new ErrorResponse(status.value(), message));
    }

    private record ErrorResponse(int status, String message) {}
}
