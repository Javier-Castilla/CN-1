package software.ulpgc.es.practicacn1.domain.repository;


import software.ulpgc.es.practicacn1.domain.model.Book;

import java.util.List;

public interface BookRepository {
    List<Book> getAllBooks();
    Book getBook(String isbn);
    boolean saveBook(Book book);
    Book deleteBook(String isbn);
    Book updateBook(Book book);
}
