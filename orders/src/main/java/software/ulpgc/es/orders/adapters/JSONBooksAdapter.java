package software.ulpgc.es.orders.adapters;

import software.ulpgc.es.orders.domain.model.Book;
import software.ulpgc.es.orders.domain.pojos.BooksGetResponse;

public class JSONBooksAdapter {
    public static Book adapt(BooksGetResponse response) {
        return new Book(response.isbn(), response.title(), response.author(), response.publisher());
    }
}
