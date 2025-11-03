package software.ulpgc.es.orders.domain.io.repository.exceptions;

public class DuplicateOrderException extends RuntimeException {
    public DuplicateOrderException(String email) {
        super("Customer with email '" + email + "' already exists.");
    }
}
