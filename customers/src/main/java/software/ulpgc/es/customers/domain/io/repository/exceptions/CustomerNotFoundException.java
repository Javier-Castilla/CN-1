package software.ulpgc.es.customers.domain.io.repository.exceptions;

public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(int id) {
        super("Customer with ID " + id + " not found.");
    }
}
