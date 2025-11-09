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
            this.customerRepository = new PostgreSQLCustomerRepository(url, user, pass);

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize CustomerRepository", e);
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
        } catch (CustomerNotFoundException e) {
            return buildError(404, e.getMessage());
        } catch (DuplicateCustomerException e) {
            return buildError(409, e.getMessage());
        } catch (CustomersDatabaseException e) {
            return buildError(500, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return buildError(500, "Unexpected error processing request: " + e.getMessage());
        }
    }

    private Object handlePost(Map<String, Object> data) {
        String name = (String) data.getOrDefault("name", "");
        String email = (String) data.getOrDefault("email", "");

        if (name.isEmpty() || email.isEmpty())
            throw new IllegalArgumentException("Incomplete data to create a customer");

        final Customer[] resultHolder = new Customer[1];
        Customer customerToSave = new Customer(0, name, email);

        new CreateCustomerCommand(() -> customerToSave, result -> resultHolder[0] = result, customerRepository).execute();
        return buildResponse(201, resultHolder[0]);
    }

    private Object handleGet(Map<String, String> pathParams) {
        if (pathParams != null && pathParams.containsKey("customerId")) {
            final Customer[] resultHolder = new Customer[1];
            int id = Integer.parseInt(pathParams.get("customerId"));
            new GetCustomerCommand(() -> id, result -> resultHolder[0] = result, customerRepository).execute();
            return buildResponse(200, resultHolder[0]);
        } else {
            List<Customer> customers = new ArrayList<>();
            new GetAllCustomersCommand(result -> customers.addAll(result), customerRepository).execute();
            return buildResponse(200, customers);
        }
    }

    private Object handlePut(Map<String, String> pathParams, Map<String, Object> data) {
        if (pathParams == null || !pathParams.containsKey("customerId")) {
            throw new IllegalArgumentException("Customer ID is required to update a customer");
        }

        int id = Integer.parseInt(pathParams.get("customerId"));
        String name = (String) data.getOrDefault("name", "");
        String email = (String) data.getOrDefault("email", "");

        final Customer[] resultHolder = new Customer[1];
        Customer customerToUpdate = new Customer(id, name, email);

        new UpdateCustomerCommand(() -> customerToUpdate, result -> resultHolder[0] = result, customerRepository).execute();
        return buildResponse(200, resultHolder[0]);
    }

    private Object handleDelete(Map<String, String> pathParams) {
        if (pathParams == null || !pathParams.containsKey("customerId")) {
            throw new IllegalArgumentException("Customer ID is required to delete a customer");
        }

        final Customer[] resultHolder = new Customer[1];
        int id = Integer.parseInt(pathParams.get("customerId"));
        new DeleteCustomerCommand(() -> id, result -> resultHolder[0] = result, customerRepository).execute();
        return buildResponse(200, resultHolder[0]);
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
