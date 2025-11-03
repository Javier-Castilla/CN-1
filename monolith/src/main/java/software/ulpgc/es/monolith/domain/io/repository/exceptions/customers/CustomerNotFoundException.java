package software.ulpgc.es.monolith.domain.io.repository.exceptions.customers;

public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(int id) {
        super("Customer with ID " + id + " not found.");
    }
}
