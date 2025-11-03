package software.ulpgc.es.monolith.domain.io.repository.exceptions.orders;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(int id) {
        super("Customer with ID " + id + " not found.");
    }
}
