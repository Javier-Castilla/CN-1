package software.ulpgc.es.monolith.domain.io.repository.exceptions.books;

public class BooksDatabaseException extends RuntimeException {
    public BooksDatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
