package software.ulpgc.es.monolith.domain.io.repository.exceptions.orders;

public class DuplicateOrderException extends RuntimeException {
    public DuplicateOrderException(String id) {
        super("Order with ID '" + id + "' already exists.");
    }
}
