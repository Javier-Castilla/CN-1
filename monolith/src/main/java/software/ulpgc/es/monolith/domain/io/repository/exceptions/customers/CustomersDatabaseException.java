package software.ulpgc.es.monolith.domain.io.repository.exceptions.customers;

public class CustomersDatabaseException extends RuntimeException {
    public CustomersDatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
