package software.ulpgc.es.orders.app.io.books;

import software.ulpgc.es.orders.domain.io.book.BookDeserializer;
import software.ulpgc.es.orders.domain.io.book.BookReader;

public class BookLoader {
    private final BookReader reader;
    private final BookDeserializer deserializer;

    public BookLoader(BookReader reader, BookDeserializer deserializer) {
        this.reader = reader;
        this.deserializer = deserializer;
    }

    public Object load(String url) {
        return this.deserializer.deserialize(this.reader.read(url));
    }
}
