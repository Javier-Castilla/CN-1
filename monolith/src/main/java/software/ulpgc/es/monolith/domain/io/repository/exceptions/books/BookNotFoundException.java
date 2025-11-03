package software.ulpgc.es.monolith.domain.io.repository.exceptions.books;

public class BookNotFoundException extends RuntimeException {
    public BookNotFoundException(String message) {
        super(message);
    }
}
