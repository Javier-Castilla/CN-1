package software.ulpgc.es.monolith.domain.io.repository.exceptions.orders;

public class OrdersDatabaseException extends RuntimeException {
    public OrdersDatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
