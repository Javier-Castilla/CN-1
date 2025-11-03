package software.ulpgc.es.monolith.domain.io.repository.exceptions.books;

public class DuplicateBookException extends RuntimeException {
    public DuplicateBookException(String message) {
        super(message);
    }
}
