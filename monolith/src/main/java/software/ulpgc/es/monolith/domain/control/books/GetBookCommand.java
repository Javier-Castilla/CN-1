package software.ulpgc.es.monolith.domain.control.books;

import software.ulpgc.es.monolith.domain.control.Command;
import software.ulpgc.es.monolith.domain.io.repository.BookRepository;
import software.ulpgc.es.monolith.domain.model.Book;
import software.ulpgc.es.monolith.domain.model.ISBN;

public class GetBookCommand implements Command {
    private final Input input;
    private final Output output;
    private final BookRepository bookRepository;

    public GetBookCommand(Input input, Output output, BookRepository bookRepository) {
        this.input = input;
        this.output = output;
        this.bookRepository = bookRepository;
    }

    @Override
    public void execute() {
        this.output.result(this.bookRepository.getBook(this.input.isbn().toString()));
    }

    public interface Input {
        ISBN isbn();
    }

    public interface Output {
        void result(Book book);
    }
}
