package software.ulpgc.es.orders.domain.io.repository.exceptions;

public class DuplicateOrderException extends RuntimeException {
    public DuplicateOrderException(String id) {
        super("Order with id '" + id + "' already exists.");
    }
}
