package software.ulpgc.es.orders.app.io.books;

import software.ulpgc.es.orders.domain.io.book.BookReader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class BooksBookReader implements BookReader {
    @Override
    public String read(String path) {
        try(InputStream in = new URL(path).openStream()) {
            return new String(in.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
