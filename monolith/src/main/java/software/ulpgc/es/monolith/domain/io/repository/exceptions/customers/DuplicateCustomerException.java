package software.ulpgc.es.monolith.domain.io.repository.exceptions.customers;

public class DuplicateCustomerException extends RuntimeException {
    public DuplicateCustomerException(String email) {
        super("Customer with email '" + email + "' already exists.");
    }
}
