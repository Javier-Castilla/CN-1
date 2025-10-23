package software.ulpgc.es.practicacn1.domain.control.commands;

import software.ulpgc.es.practicacn1.domain.control.Command;
import software.ulpgc.es.practicacn1.domain.model.Book;
import software.ulpgc.es.practicacn1.domain.repository.BookRepository;

import java.util.HashMap;
import java.util.Map;

public class CreateBookCommand implements Command {
    private final Input input;
    private final Output output;
    private final BookRepository repository;

    public CreateBookCommand(Input input, Output output, BookRepository repository) {
        this.repository = repository;
        this.input = input;
        this.output = output;
    }

    @Override
    public void execute() {
        if (repository.saveBook(this.input.book())) this.output.result(buildResult(this.input.book()));
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
        Book book();
    }

    public interface Output {
        void result(Map<String, Object> result);
    }
}
