package software.ulpgc.es.books.domain.control;

import software.ulpgc.es.books.domain.io.repository.BookRepository;
import software.ulpgc.es.books.domain.model.Book;

public class CreateBookCommand implements Command {
    private final Input input;
    private final Output output;
    private final BookRepository bookRepository;

    public CreateBookCommand(Input input, Output output, BookRepository bookRepository) {
        this.input = input;
        this.output = output;
        this.bookRepository = bookRepository;
    }

    @Override
    public void execute() {
        boolean saved = this.bookRepository.saveBook(this.input.book());
        this.output.result(saved ? this.input.book() : null);
    }

    public interface Input {
        Book book();
    }

    public interface Output {
        void result(Book book);
    }
}
