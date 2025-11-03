package software.ulpgc.es.monolith.domain.io.repository.exceptions.books;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String isbn, int requested, int available) {
        super("Not enough stock for book " + isbn + ": requested " + requested + ", available " + available);
    }
}
