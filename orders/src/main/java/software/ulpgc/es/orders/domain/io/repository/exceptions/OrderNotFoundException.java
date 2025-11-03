package software.ulpgc.es.orders.domain.io.repository.exceptions;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(int id) {
        super("Customer with ID " + id + " not found.");
    }
}
