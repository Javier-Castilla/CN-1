package software.ulpgc.es.customers.domain.io.repository.exceptions;

public class CustomersDatabaseException extends RuntimeException {
    public CustomersDatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
