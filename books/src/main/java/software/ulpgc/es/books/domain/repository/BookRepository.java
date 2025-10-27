package software.ulpgc.es.books.domain.repository;


import software.ulpgc.es.books.domain.model.Book;

import java.util.List;

public interface BookRepository {
    List<Book> getAllBooks();
    Book getBook(String isbn);
    boolean saveBook(Book book);
    Book deleteBook(String isbn);
    Book updateBook(Book book);
}
