package software.ulpgc.es.books.domain.control;

import software.ulpgc.es.books.domain.io.repository.BookRepository;
import software.ulpgc.es.books.domain.model.Book;

import java.util.List;

public class GetAllBooksCommand implements Command {
    private final Output output;
    private final BookRepository bookRepository;

    public GetAllBooksCommand(Output output, BookRepository bookRepository) {
        this.output = output;
        this.bookRepository = bookRepository;
    }

    @Override
    public void execute() {
        this.output.result(this.bookRepository.getAllBooks());
    }

    public interface Output {
        void result(List<Book> books);
    }
}
