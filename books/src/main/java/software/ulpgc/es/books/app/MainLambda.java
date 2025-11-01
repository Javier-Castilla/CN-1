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
            String user = System.getenv("DB_USERNAME");
            String pass = System.getenv("DB_PASSWORD");
            String url = String.format("jdbc:%s://%s:%s/%s", type, host, port, dbName);
            this.bookRepository = PostgreSQLBookRepository.getInstance(url, user, pass);
        } catch (Exception e) {
            throw new RuntimeException("Error al inicializar BookRepository", e);
        }
    }

    @Override
    public Object handleRequest(Map<String,Object> input, Context context) {
        try {
            String method = (String) input.get("httpMethod");
            String body = (String) input.get("body");
            Map<String,Object> data = (body != null && !body.isEmpty())
                    ? mapper.readValue(body, Map.class)
                    : Map.of();
            Book[] resultHandler = new Book[1];
            switch (method) {
                case "POST":
                    return handlePost(data, resultHandler);
                case "GET":
                    Map<String,String> queryParams = (Map<String,String>) input.get("queryStringParameters");
                    return handleGet(queryParams, resultHandler);
                case "PUT":
                    return handlePut(data, resultHandler);
                case "DELETE":
                    Map<String,String> deleteParams = (Map<String,String>) input.get("queryStringParameters");
                    return handleDelete(deleteParams, resultHandler);
                default:
                    throw new IllegalArgumentException("Método no soportado: " + method);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error procesando la petición: " + e.getMessage(), e);
        }
    }

    private Object handlePost(Map<String,Object> data, Book[] resultHandler) {
        if (!data.containsKey("isbn") || !data.containsKey("title")) throw new IllegalArgumentException("Datos incompletos para crear un libro");
        Book bookToSave = new Book(
                new ISBN((String) data.get("isbn")),
                (String) data.get("title"),
                (String) data.getOrDefault("author", ""),
                (String) data.getOrDefault("publisher", ""),
                parseStock(data.get("stock"))
        );
        new CreateBookCommand(() -> bookToSave, result -> resultHandler[0] = result, bookRepository).execute();
        return resultHandler[0];
    }

    private Object handleGet(Map<String,String> queryParams, Book[] resultHandler) {
        if (queryParams != null && queryParams.get("id") != null) {
            new GetBookCommand(() -> new ISBN(queryParams.get("id")), result -> resultHandler[0] = result, bookRepository).execute();
            return resultHandler[0];
        } else {
            List<Book> books = new ArrayList<>();
            new GetAllBooksCommand(books::addAll, bookRepository).execute();
            return books;
        }
    }

    private Object handlePut(Map<String,Object> data, Book[] resultHandler) {
        if (!data.containsKey("isbn")) throw new IllegalArgumentException("ISBN es obligatorio para actualizar un libro");
        Book bookToUpdate = new Book(
                new ISBN((String) data.get("isbn")),
                (String) data.getOrDefault("title", ""),
                (String) data.getOrDefault("author", ""),
                (String) data.getOrDefault("publisher", ""),
                parseStock(data.get("stock"))
        );
        new UpdateBookCommand(() -> bookToUpdate, result -> resultHandler[0] = result, bookRepository).execute();
        return resultHandler[0];
    }

    private Object handleDelete(Map<String,String> queryParams, Book[] resultHandler) {
        if (queryParams == null || queryParams.get("id") == null) throw new IllegalArgumentException("ID es obligatorio para eliminar un libro");
        new DeleteBookCommand(() -> new ISBN(queryParams.get("id")), result -> resultHandler[0] = result, bookRepository).execute();
        return resultHandler[0];
    }

    private int parseStock(Object stockObj) {
        if (stockObj == null) return 0;
        if (stockObj instanceof Number) return ((Number) stockObj).intValue();
        try {
            return Integer.parseInt(stockObj.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
