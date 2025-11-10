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
            this.bookRepository = new PostgreSQLBookRepository(url, user, pass);

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize BookRepository", e);
        }
    }

    @Override
    public Object handleRequest(Map<String, Object> input, Context context) {
        try {
            String method = (String) input.get("httpMethod");
            String body = (String) input.get("body");

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (body != null && !body.isEmpty())
                    ? mapper.readValue(body, Map.class)
                    : Map.of();

            return switch (method) {
                case "POST" -> handlePost(data);
                case "GET" -> {
                    @SuppressWarnings("unchecked")
                    Map<String, String> pathParams = (Map<String, String>) input.get("pathParameters");
                    yield handleGet(pathParams);
                }
                case "PUT" -> {
                    @SuppressWarnings("unchecked")
                    Map<String, String> pathParams = (Map<String, String>) input.get("pathParameters");
                    yield handlePut(pathParams, data);
                }
                case "DELETE" -> {
                    @SuppressWarnings("unchecked")
                    Map<String, String> pathParams = (Map<String, String>) input.get("pathParameters");
                    yield handleDelete(pathParams);
                }
                case "OPTIONS" -> buildCorsPreflightResponse();
                default -> buildError(400, "Unsupported method: " + method);
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
            return buildError(500, "Unexpected error processing request: " + e.getMessage());
        }
    }

    private Object handlePost(Map<String, Object> data) {
        if (!data.containsKey("isbn") || !data.containsKey("title"))
            throw new IllegalArgumentException("Incomplete data to create a book");

        final Book[] resultHolder = new Book[1];

        String isbnValue = extractIsbn(data.get("isbn"));

        Book bookToSave = new Book(
                new ISBN(isbnValue),
                (String) data.get("title"),
                (String) data.getOrDefault("author", ""),
                (String) data.getOrDefault("publisher", ""),
                parseStock(data.get("stock"))
        );

        new CreateBookCommand(() -> bookToSave, result -> resultHolder[0] = result, bookRepository).execute();
        return buildResponse(201, resultHolder[0]);
    }

    private Object handleGet(Map<String, String> pathParams) {
        if (pathParams != null && pathParams.containsKey("id")) {
            final Book[] resultHolder = new Book[1];
            String isbn = pathParams.get("id");
            new GetBookCommand(() -> new ISBN(isbn), result -> resultHolder[0] = result, bookRepository).execute();
            return buildResponse(200, resultHolder[0]);
        } else {
            List<Book> books = new ArrayList<>();
            new GetAllBooksCommand(books::addAll, bookRepository).execute();
            return buildResponse(200, books);
        }
    }

    private Object handlePut(Map<String, String> pathParams, Map<String, Object> data) {
        if (pathParams == null || !pathParams.containsKey("id")) {
            throw new IllegalArgumentException("ISBN is required to update a book");
        }

        final Book[] resultHolder = new Book[1];
        String isbn = pathParams.get("id");
        Book bookToUpdate = new Book(
                new ISBN(isbn),
                (String) data.getOrDefault("title", ""),
                (String) data.getOrDefault("author", ""),
                (String) data.getOrDefault("publisher", ""),
                parseStock(data.get("stock"))
        );

        new UpdateBookCommand(() -> bookToUpdate, result -> resultHolder[0] = result, bookRepository).execute();
        return buildResponse(200, resultHolder[0]);
    }

    private Object handleDelete(Map<String, String> pathParams) {
        if (pathParams == null || !pathParams.containsKey("id")) {
            throw new IllegalArgumentException("ISBN is required to delete a book");
        }

        final Book[] resultHolder = new Book[1];
        String isbn = pathParams.get("id");
        new DeleteBookCommand(() -> new ISBN(isbn), result -> resultHolder[0] = result, bookRepository).execute();
        return buildResponse(200, resultHolder[0]);
    }

    private String extractIsbn(Object isbnObj) {
        if (isbnObj instanceof String) {
            return (String) isbnObj;
        } else if (isbnObj instanceof Map) {
            Object value = ((Map<?, ?>) isbnObj).get("value");
            return value != null ? value.toString() : null;
        }
        return isbnObj != null ? isbnObj.toString() : null;
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

    private Map<String, Object> buildResponse(int statusCode, Object body) {
        return Map.of(
                "statusCode", statusCode,
                "headers", corsHeaders(),
                "body", toJson(body)
        );
    }

    private Map<String, Object> buildError(int statusCode, String message) {
        ErrorResponse error = new ErrorResponse(statusCode, message);
        return Map.of(
                "statusCode", statusCode,
                "headers", corsHeaders(),
                "body", toJson(error)
        );
    }

    private Map<String, String> corsHeaders() {
        return Map.of(
                "Content-Type", "application/json",
                "Access-Control-Allow-Origin", "*",
                "Access-Control-Allow-Headers", "Content-Type,x-api-key",
                "Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS"
        );
    }

    private Map<String, Object> buildCorsPreflightResponse() {
        return Map.of(
                "statusCode", 200,
                "headers", corsHeaders(),
                "body", ""
        );
    }

    private String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{\"error\":\"Error serializing response\"}";
        }
    }

    private record ErrorResponse(int status, String message) {}
}
