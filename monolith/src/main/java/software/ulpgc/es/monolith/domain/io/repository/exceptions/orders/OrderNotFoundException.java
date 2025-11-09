package software.ulpgc.es.monolith.domain.io.repository.exceptions.orders;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(int id) {
        super("Order with  ID " + id + " not found.");
    }
}
