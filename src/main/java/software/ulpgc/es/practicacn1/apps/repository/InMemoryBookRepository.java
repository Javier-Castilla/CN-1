package software.ulpgc.es.practicacn1.apps;

import software.ulpgc.es.practicacn1.domain.model.Book;
import software.ulpgc.es.practicacn1.domain.repository.BookRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryBookRepository implements BookRepository {
    private final Map<String, Book> books;

    public InMemoryBookRepository() {
        this.books = new HashMap<>();
    }

    @Override
    public List<Book> getAllBooks() {
        return List.of();
    }

    @Override
    public Book getBook(String isbn) {
        return null;
    }

    @Override
    public boolean saveBook(Book book) {
        return true;
    }

    @Override
    public Book deleteBook(String isbn) {

        return null;
    }

    @Override
    public void updateBook(Book book) {

    }
}
