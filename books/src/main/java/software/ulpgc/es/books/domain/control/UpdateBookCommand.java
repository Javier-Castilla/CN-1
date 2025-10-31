package software.ulpgc.es.books.domain.control;

import software.ulpgc.es.books.domain.io.repository.BookRepository;
import software.ulpgc.es.books.domain.model.Book;

public class UpdateBookCommand implements Command {
    private final Input input;
    private final Output output;
    private final BookRepository bookRepository;

    public UpdateBookCommand(Input input, Output output, BookRepository bookRepository) {
        this.input = input;
        this.output = output;
        this.bookRepository = bookRepository;
    }

    @Override
    public void execute() {
        this.output.result(this.bookRepository.updateBook(this.input.book()));
    }

    public interface Input {
        Book book();
    }

    public interface Output {
        void result(Book book);
    }
}
