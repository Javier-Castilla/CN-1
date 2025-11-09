package software.ulpgc.es.orders.domain.io.repository.exceptions;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(int orderId) {
        super("Order with ID " + orderId + " not found.");
    }
}
