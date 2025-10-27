package software.ulpgc.es.practicacn1.repository;

import software.ulpgc.es.books.domain.repository.BookRepository;
import software.ulpgc.es.books.domain.model.Book;

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
        return this.books.get(isbn);
    }

    @Override
    public boolean saveBook(Book book) {
        return this.books.put(book.isbn(), book) != null;
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
