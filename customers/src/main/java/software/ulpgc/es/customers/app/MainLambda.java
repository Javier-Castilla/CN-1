package software.ulpgc.es.customers.app;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.ulpgc.es.customers.app.io.repository.PostgreSQLCustomerRepository;
import software.ulpgc.es.customers.domain.control.*;
import software.ulpgc.es.customers.domain.io.repository.CustomerRepository;
import software.ulpgc.es.customers.domain.io.repository.exceptions.*;
import software.ulpgc.es.customers.domain.model.Customer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainLambda implements RequestHandler<Map<String, Object>, Object> {

    private final ObjectMapper mapper = new ObjectMapper();
    private final CustomerRepository customerRepository;

    public MainLambda() {
        try {
            String type = System.getenv("DB_TYPE");
            String host = System.getenv("DB_HOST");
            String port = System.getenv("DB_PORT");
            String dbName = System.getenv("DB_NAME");
            String user = System.getenv("DB_USERNAME");
            String pass = System.getenv("DB_PASSWORD");
            String url = String.format("jdbc:%s://%s:%s/%s", type, host, port, dbName);
            this.customerRepository = PostgreSQLCustomerRepository.getInstance(url, user, pass);
        } catch (Exception e) {
            throw new RuntimeException("Error al inicializar CustomerRepository", e);
        }
    }

    @Override
    public Object handleRequest(Map<String, Object> input, Context context) {
        try {
            String method = (String) input.get("httpMethod");
            String body = (String) input.get("body");
            Map<String, Object> data = (body != null && !body.isEmpty()) ? mapper.readValue(body, Map.class) : Map.of();
            Customer[] resultHandler = new Customer[1];

            switch (method) {
                case "POST" -> {
                    return handlePost(data, resultHandler);
                }
                case "GET" -> {
                    Map<String, String> queryParams = (Map<String, String>) input.get("queryStringParameters");
                    return handleGet(queryParams, resultHandler);
                }
                case "PUT" -> {
                    return handlePut(data, resultHandler);
                }
                case "DELETE" -> {
                    Map<String, String> deleteParams = (Map<String, String>) input.get("queryStringParameters");
                    return handleDelete(deleteParams, resultHandler);
                }
                case "OPTIONS" -> {
                    return buildCorsPreflightResponse(); // 👈 importante para preflight
                }
                default -> {
                    return buildError(400, "Método no soportado: " + method);
                }
            }
        } catch (IllegalArgumentException e) {
            return buildError(400, e.getMessage());
        } catch (CustomerNotFoundException e) {
            return buildError(404, e.getMessage());
        } catch (DuplicateCustomerException e) {
            return buildError(409, e.getMessage());
        } catch (CustomersDatabaseException e) {
            return buildError(500, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return buildError(500, "Error inesperado procesando la petición: " + e.getMessage());
        }
    }

    private Object handlePost(Map<String, Object> data, Customer[] resultHandler) {
        String name = (String) data.getOrDefault("name", "");
        String email = (String) data.getOrDefault("email", "");
        if (name.isEmpty() || email.isEmpty()) throw new IllegalArgumentException("Datos incompletos para crear un cliente");

        Customer customerToSave = new Customer(0, name, email);
        new CreateCustomerCommand(() -> customerToSave, result -> resultHandler[0] = result, customerRepository).execute();
        return buildResponse(201, resultHandler[0]);
    }

    private Object handleGet(Map<String, String> queryParams, Customer[] resultHandler) {
        if (queryParams != null && queryParams.get("id") != null) {
            int id = Integer.parseInt(queryParams.get("id"));
            new GetCustomerCommand(() -> id, result -> resultHandler[0] = result, customerRepository).execute();
            return buildResponse(200, resultHandler[0]);
        } else {
            List<Customer> customers = new ArrayList<>();
            new GetAllCustomersCommand(result -> customers.addAll(result), customerRepository).execute();
            return buildResponse(200, customers);
        }
    }

    private Object handlePut(Map<String, Object> data, Customer[] resultHandler) {
        int id = parseId(data.get("id"));
        String name = (String) data.getOrDefault("name", "");
        String email = (String) data.getOrDefault("email", "");
        if (id == 0) throw new IllegalArgumentException("ID es obligatorio para actualizar un cliente");

        Customer customerToUpdate = new Customer(id, name, email);
        new UpdateCustomerCommand(() -> customerToUpdate, result -> resultHandler[0] = result, customerRepository).execute();
        return buildResponse(200, resultHandler[0]);
    }

    private Object handleDelete(Map<String, String> deleteParams, Customer[] resultHandler) {
        if (deleteParams == null || deleteParams.get("id") == null)
            throw new IllegalArgumentException("ID es obligatorio para eliminar un cliente");

        int deleteId = Integer.parseInt(deleteParams.get("id"));
        new DeleteCustomerCommand(() -> deleteId, result -> resultHandler[0] = result, customerRepository).execute();
        return buildResponse(200, resultHandler[0]);
    }

    private int parseId(Object idObj) {
        if (idObj == null) return 0;
        if (idObj instanceof Number) return ((Number) idObj).intValue();
        try {
            return Integer.parseInt(idObj.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // ============================================================
    // Métodos auxiliares con CORS
    // ============================================================

    private Map<String, Object> buildResponse(int statusCode, Object body) {
        return Map.of(
                "statusCode", statusCode,
                "headers", corsHeaders(), // 👈 cabeceras CORS añadidas
                "body", toJson(body)
        );
    }

    private Map<String, Object> buildError(int statusCode, String message) {
        ErrorResponse error = new ErrorResponse(statusCode, message);
        return Map.of(
                "statusCode", statusCode,
                "headers", corsHeaders(), // 👈 también aquí
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
        // Respuesta directa a las solicitudes OPTIONS del navegador
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
            return "{\"error\":\"Error serializando respuesta\"}";
        }
    }

    private record ErrorResponse(int status, String message) {}
}
