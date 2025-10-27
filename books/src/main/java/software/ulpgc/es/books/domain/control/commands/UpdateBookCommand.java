package software.ulpgc.es.books.domain.control.commands;

import software.ulpgc.es.books.domain.control.Command;
import software.ulpgc.es.books.domain.model.Book;
import software.ulpgc.es.books.domain.repository.BookRepository;

import java.util.Map;

public class UpdateBookCommand implements Command {
    private final Input input;
    private final Output output;
    private final BookRepository repository;

    public UpdateBookCommand(Input input, Output output, BookRepository repository) {
        this.input = input;
        this.output = output;
        this.repository = repository;
    }

    @Override
    public void execute() {
        String isbn = input.isbn();
        Book existingBook = repository.getBook(isbn);

        if (existingBook == null) {
            output.result(Map.of(
                    "success", false,
                    "message", "Book not found with ISBN: " + isbn
            ));
            return;
        }

        Book updatedBook = new Book(
                existingBook.isbn(),
                input.title() != null ? input.title() : existingBook.title(),
                input.author() != null ? input.author() : existingBook.author(),
                input.publisher() != null ? input.publisher() : existingBook.publisher()
        );

        repository.updateBook(updatedBook);

        output.result(Map.of(
                "success", true,
                "message", "Book updated successfully",
                "book", updatedBook
        ));
    }

    public interface Input {
        String isbn();
        String title();
        String author();
        String publisher();
    }

    public interface Output {
        void result(Map<String, Object> result);
    }
}
