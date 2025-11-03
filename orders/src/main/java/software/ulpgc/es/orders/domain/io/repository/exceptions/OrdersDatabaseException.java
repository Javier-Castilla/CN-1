package software.ulpgc.es.orders.domain.io.repository.exceptions;

public class OrdersDatabaseException extends RuntimeException {
    public OrdersDatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
