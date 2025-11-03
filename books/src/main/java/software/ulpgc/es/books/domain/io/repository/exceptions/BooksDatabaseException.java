package software.ulpgc.es.books.domain.io.repository.exceptions;

public class BooksDatabaseException extends RuntimeException {
    public BooksDatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
