package software.ulpgc.es.practicacn1.domain;

import java.awt.print.Book;

public interface BookRepository {
    Book getBook(String isbn);
    void saveBook(Book book);
    void deleteBook(String isbn);
    void updateBook(Book book);
}
