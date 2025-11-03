package software.ulpgc.es.orders.domain.io.repository.exceptions;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String isbn, int requested, int available) {
        super("Not enough stock for book " + isbn + ": requested " + requested + ", available " + available);
    }
}
