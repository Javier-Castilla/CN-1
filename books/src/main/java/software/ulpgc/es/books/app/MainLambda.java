package software.ulpgc.es.books.app;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.ulpgc.es.books.app.io.repository.PostgreSQLBookRepository;
import software.ulpgc.es.books.domain.control.*;
import software.ulpgc.es.books.domain.io.repository.BookRepository;
import software.ulpgc.es.books.domain.io.repository.exceptions.*;
import software.ulpgc.es.books.domain.model.Book;
import software.ulpgc.es.books.domain.model.ISBN;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainLambda implements RequestHandler<Map<String, Object>, Object> {

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
    public Object handleRequest(Map<String, Object> input, Context context) {
        try {
            String method = (String) input.get("httpMethod");
            String body = (String) input.get("body");
            Map<String, Object> data = (body != null && !body.isEmpty())
                    ? mapper.readValue(body, Map.class)
                    : Map.of();

            Book[] resultHandler = new Book[1];

            return switch (method) {
                case "POST" -> handlePost(data, resultHandler);
                case "GET" -> {
                    @SuppressWarnings("unchecked")
                    Map<String, String> queryParams = (Map<String, String>) input.get("queryStringParameters");
                    yield handleGet(queryParams, resultHandler);
                }
                case "PUT" -> handlePut(data, resultHandler);
                case "DELETE" -> {
                    @SuppressWarnings("unchecked")
                    Map<String, String> deleteParams = (Map<String, String>) input.get("queryStringParameters");
                    yield handleDelete(deleteParams, resultHandler);
                }
                default -> buildError(400, "Método no soportado: " + method);
            };

        } catch (IllegalArgumentException e) {
            return buildError(400, e.getMessage());
        } catch (BookNotFoundException e) {
            return buildError(404, e.getMessage());
        } catch (DuplicateBookException e) {
            return buildError(409, e.getMessage());
        } catch (BooksDatabaseException e) {
            return buildError(500, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return buildError(500, "Error inesperado procesando la petición: " + e.getMessage());
        }
    }

    private Object handlePost(Map<String, Object> data, Book[] resultHandler) {
        if (!data.containsKey("isbn") || !data.containsKey("title"))
            throw new IllegalArgumentException("Datos incompletos para crear un libro");

        Book bookToSave = new Book(
                new ISBN((String) data.get("isbn")),
                (String) data.get("title"),
                (String) data.getOrDefault("author", ""),
                (String) data.getOrDefault("publisher", ""),
                parseStock(data.get("stock"))
        );

        new CreateBookCommand(() -> bookToSave, result -> resultHandler[0] = result, bookRepository).execute();
        return buildResponse(201, resultHandler[0]);
    }

    private Object handleGet(Map<String, String> queryParams, Book[] resultHandler) {
        if (queryParams != null && queryParams.get("isbn") != null) {
            new GetBookCommand(() -> new ISBN(queryParams.get("isbn")), result -> resultHandler[0] = result, bookRepository).execute();
            return buildResponse(200, resultHandler[0]);
        } else {
            List<Book> books = new ArrayList<>();
            new GetAllBooksCommand(books::addAll, bookRepository).execute();
            return buildResponse(200, books);
        }
    }

    private Object handlePut(Map<String, Object> data, Book[] resultHandler) {
        if (!data.containsKey("isbn"))
            throw new IllegalArgumentException("ISBN es obligatorio para actualizar un libro");

        Book bookToUpdate = new Book(
                new ISBN((String) data.get("isbn")),
                (String) data.getOrDefault("title", ""),
                (String) data.getOrDefault("author", ""),
                (String) data.getOrDefault("publisher", ""),
                parseStock(data.get("stock"))
        );

        new UpdateBookCommand(() -> bookToUpdate, result -> resultHandler[0] = result, bookRepository).execute();
        return buildResponse(200, resultHandler[0]);
    }

    private Object handleDelete(Map<String, String> queryParams, Book[] resultHandler) {
        if (queryParams == null || queryParams.get("isbn") == null)
            throw new IllegalArgumentException("ISBN es obligatorio para eliminar un libro");

        new DeleteBookCommand(() -> new ISBN(queryParams.get("isbn")), result -> resultHandler[0] = result, bookRepository).execute();
        return buildResponse(200, resultHandler[0]);
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

    // ============================================================
    // Métodos auxiliares
    // ============================================================
    private Map<String, Object> buildResponse(int statusCode, Object body) {
        return Map.of(
                "statusCode", statusCode,
                "headers", Map.of("Content-Type", "application/json"),
                "body", toJson(body)
        );
    }

    private Map<String, Object> buildError(int statusCode, String message) {
        ErrorResponse error = new ErrorResponse(statusCode, message);
        return Map.of(
                "statusCode", statusCode,
                "headers", Map.of("Content-Type", "application/json"),
                "body", toJson(error)
        );
    }

    private String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{\"error\":\"Error serializando respuesta\"}";
        }
    }

    private record ErrorResponse(int status, String message) {}
}
