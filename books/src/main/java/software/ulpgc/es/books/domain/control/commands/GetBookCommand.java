package software.ulpgc.es.books.domain.control.commands;

import software.ulpgc.es.books.domain.control.Command;
import software.ulpgc.es.books.domain.model.Book;
import software.ulpgc.es.books.domain.repository.BookRepository;

import java.util.HashMap;
import java.util.Map;

public class GetBookCommand implements Command {
    private final Input input;
    private final Output output;
    private final BookRepository repository;

    public GetBookCommand(Input input, Output output, BookRepository repository) {
        this.input = input;
        this.output = output;
        this.repository = repository;
    }

    @Override
    public void execute() {
        this.output.result(buildResult(this.repository.getBook(this.input.isbn())));
    }

    private Map<String, Object> buildResult(Book book) {
        Map<String, Object> result = new HashMap<>();
        result.put("isbn", book.isbn());
        result.put("title", book.title());
        result.put("author", book.author());
        result.put("publisher", book.publisher());
        return result;
    }

    public interface Input {
        String isbn();
    }

    public interface Output {
        void result(Map<String, Object> books);
    }
}
