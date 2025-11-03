package software.ulpgc.es.monolith.domain.io.repository.exceptions.orders;

public class DuplicateOrderException extends RuntimeException {
    public DuplicateOrderException(String email) {
        super("Customer with email '" + email + "' already exists.");
    }
}
