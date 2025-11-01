package software.ulpgc.es.customers.app;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.ulpgc.es.customers.app.io.repository.PostgreSQLCustomerRepository;
import software.ulpgc.es.customers.domain.control.*;
import software.ulpgc.es.customers.domain.io.repository.CustomerRepository;
import software.ulpgc.es.customers.domain.model.Customer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainLambda implements RequestHandler<Map<String,Object>, Object> {

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
    public Object handleRequest(Map<String,Object> input, Context context) {
        try {
            String method = (String) input.get("httpMethod");
            String body = (String) input.get("body");
            Map<String,Object> data = (body != null && !body.isEmpty()) ? mapper.readValue(body, Map.class) : Map.of();
            Customer[] resultHandler = new Customer[1];

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

    private Object handlePost(Map<String,Object> data, Customer[] resultHandler) {
        int id = parseId(data.get("id"));
        String name = (String) data.getOrDefault("name", "");
        String email = (String) data.getOrDefault("email", "");

        Customer customerToSave = new Customer(id, name, email);
        new CreateCustomerCommand(() -> customerToSave, result -> resultHandler[0] = result, customerRepository).execute();
        return resultHandler[0];
    }

    private Object handleGet(Map<String,String> queryParams, Customer[] resultHandler) {
        if (queryParams != null && queryParams.get("id") != null) {
            int id = Integer.parseInt(queryParams.get("id"));
            new GetCustomerCommand(() -> id, result -> resultHandler[0] = result, customerRepository).execute();
            return resultHandler[0];
        } else {
            List<Customer> customers = new ArrayList<>();
            new GetAllCustomersCommand(result -> customers.addAll(result), customerRepository).execute();
            return customers;
        }
    }

    private Object handlePut(Map<String,Object> data, Customer[] resultHandler) {
        int id = parseId(data.get("id"));
        String name = (String) data.getOrDefault("name", "");
        String email = (String) data.getOrDefault("email", "");

        Customer customerToUpdate = new Customer(id, name, email);
        new UpdateCustomerCommand(() -> customerToUpdate, result -> resultHandler[0] = result, customerRepository).execute();
        return resultHandler[0];
    }

    private Object handleDelete(Map<String,String> deleteParams, Customer[] resultHandler) {
        if (deleteParams == null || deleteParams.get("id") == null) {
            throw new IllegalArgumentException("ID es obligatorio para eliminar un cliente");
        }
        int deleteId = Integer.parseInt(deleteParams.get("id"));
        new DeleteCustomerCommand(() -> deleteId, result -> resultHandler[0] = result, customerRepository).execute();
        return resultHandler[0];
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
}
