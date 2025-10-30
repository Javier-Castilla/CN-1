package software.ulpgc.es.monolith.app.repository;

import software.ulpgc.es.monolith.domain.io.repository.BookRepository;
import software.ulpgc.es.monolith.domain.model.Book;

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
        return this.books.values().stream().toList();
    }

    @Override
    public Book getBook(String isbn) {
        return this.books.get(isbn);
    }

    @Override
    public boolean saveBook(Book book) {
        if (this.books.containsKey(book.isbn())) return false;
        return this.books.put(book.isbn(), book) == null;
    }

    @Override
    public Book deleteBook(String isbn) {
        return this.books.remove(isbn);
    }

    @Override
    public Book updateBook(Book book) {
        return this.books.put(book.isbn(), book);
    }
}
