package software.ulpgc.es.orders.app.io.books;

import com.google.gson.Gson;
import software.ulpgc.es.orders.domain.io.book.BookDeserializer;
import software.ulpgc.es.orders.domain.pojos.BooksGetResponse;

public class BooksBookDeserializer implements BookDeserializer {
    @Override
    public Object deserialize(String string) {
        return new Gson().fromJson(string, BooksGetResponse.class);
    }
}
