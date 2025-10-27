package software.ulpgc.es.books.domain.control.commands;

import software.ulpgc.es.books.domain.control.Command;
import software.ulpgc.es.books.domain.model.Book;
import software.ulpgc.es.books.domain.repository.BookRepository;

import java.util.List;
import java.util.Map;

public class GetAllBooksCommand implements Command {

    private final Output output;
    private final BookRepository repository;

    public GetAllBooksCommand(BookRepository repository, Output output) {
        this.repository = repository;
        this.output = output;
    }

    @Override
    public void execute() {
        List<Book> books = repository.getAllBooks();
        output.result(Map.of(
                "success", true,
                "books", books
        ));
    }

    public interface Output {
        void result(Map<String, Object> result);
    }
}
