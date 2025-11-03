package software.ulpgc.es.customers.domain.io.repository.exceptions;

public class DuplicateCustomerException extends RuntimeException {
    public DuplicateCustomerException(String email) {
        super("Customer with email '" + email + "' already exists.");
    }
}
