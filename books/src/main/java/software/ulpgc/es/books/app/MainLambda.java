package software.ulpgc.es.books.app;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.ulpgc.es.books.app.io.repository.PostgreSQLBookRepository;
import software.ulpgc.es.books.domain.control.*;
import software.ulpgc.es.books.domain.io.repository.BookRepository;
import software.ulpgc.es.books.domain.model.Book;
import software.ulpgc.es.books.domain.model.ISBN;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainLambda implements RequestHandler<Map<String,Object>, Object> {

    private final ObjectMapper mapper = new ObjectMapper();
    private final BookRepository bookRepository;

    public MainLambda() {
        try {
            String type = System.getenv("DB_TYPE");
            String host = System.getenv("DB_HOST");
            String port = System.getenv("DB_PORT");
            String dbName = System.getenv("DB_NAME");
            String user = System.getenv("DB_USER");
            String pass = System.getenv("DB_PASS");
            String url = String.format("jdbc:%s://%s:%s/%s", type, host, port, dbName);
            this.bookRepository = new PostgreSQLBookRepository(url, user, pass);
        } catch (Exception e) {
            throw new RuntimeException("Error al inicializar BookRepository", e);
        }
    }

    @Override
    public Object handleRequest(Map<String,Object> input, Context context) {
        try {
            String method = (String) input.get("httpMethod");
            String path = (String) input.get("path");
            String body = (String) input.get("body");
            Map<String,Object> data = body != null ? mapper.readValue(body, Map.class) : Map.of();
            Book[] resultHandler = new Book[1];
            switch (method) {
                case "POST":
                    Book bookToSave = new Book(new ISBN((String) data.get("isbn")), (String) data.get("title"), (String) data.get("author"), (String) data.get("publisher"));
                    new CreateBookCommand(() -> bookToSave, result -> resultHandler[0] = result, this.bookRepository).execute();
                    return resultHandler[0];
                case "GET":
                    String stringId = ((Map<String,String>) input.get("queryStringParameters")).get("id");
                    if (stringId != null) {
                        ISBN id = new ISBN(stringId);
                        new GetBookCommand(() -> id, result -> resultHandler[0] = result, bookRepository).execute();
                        return resultHandler[0];
                    } else {
                        List<Book> books = new ArrayList<>();
                        new GetAllBooksCommand(result -> books.addAll(result), this.bookRepository).execute();
                        return books;
                    }
                case "PUT":
                    Book bookToUpdate = new Book(new ISBN((String) data.get("isbn")), (String) data.get("title"), (String) data.get("author"), (String) data.get("publisher"));
                    new UpdateBookCommand(() -> bookToUpdate, result -> resultHandler[0] = result, this.bookRepository).execute();
                    return resultHandler[0];
                case "DELETE":
                    ISBN deleteId = new ISBN(((Map<String,String>) input.get("queryStringParameters")).get("id"));
                    new DeleteBookCommand(() -> deleteId, result -> resultHandler[0] = result, this.bookRepository).execute();
                    return resultHandler[0];
                default:
                    throw new IllegalArgumentException("Método no soportado: " + method);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}