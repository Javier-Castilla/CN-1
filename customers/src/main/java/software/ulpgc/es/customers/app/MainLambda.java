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
            String user = System.getenv("DB_USER");
            String pass = System.getenv("DB_PASS");
            String url = String.format("jdbc:%s://%s:%s/%s", type, host, port, dbName);
            this.customerRepository = new PostgreSQLCustomerRepository(url, user, pass);
        } catch (Exception e) {
            throw new RuntimeException("Error al inicializar CustomerRepository", e);
        }
    }

    @Override
    public Object handleRequest(Map<String,Object> input, Context context) {
        try {
            String method = (String) input.get("httpMethod");
            String path = (String) input.get("path");
            String body = (String) input.get("body");
            Map<String,Object> data = body != null ? mapper.readValue(body, Map.class) : Map.of();
            Customer[] resultHandler = new Customer[1];
            switch (method) {
                case "POST":
                    Customer customerToSave = new Customer((int) data.get("id"), (String) data.get("name"), (String) data.get("email"));
                    new CreateCustomerCommand(() -> customerToSave, result -> resultHandler[0] = result, this.customerRepository).execute();
                    return resultHandler[0];
                case "GET":
                    Integer id = ((Map<String,Integer>) input.get("queryStringParameters")).get("id");
                    if (id != null) {
                        new GetCustomerCommand(() -> id, result -> resultHandler[0] = result, customerRepository).execute();
                        return resultHandler[0];
                    } else {
                        List<Customer> customers = new ArrayList<>();
                        new GetAllCustomersCommand(result -> customers.addAll(result), this.customerRepository).execute();
                        return customers;
                    }
                case "PUT":
                    Customer customerToUpdate = new Customer((Integer) data.get("id"), (String) data.get("name"), (String) data.get("email"));
                    new UpdateCustomerCommand(() -> customerToUpdate, result -> resultHandler[0] = result, this.customerRepository).execute();
                    return resultHandler[0];
                case "DELETE":
                    Integer deleteId = ((Map<String, Integer>) input.get("queryStringParameters")).get("id");
                    new DeleteCustomerCommand(() -> deleteId, result -> resultHandler[0] = result, this.customerRepository).execute();
                    return resultHandler[0];
                default:
                    throw new IllegalArgumentException("Método no soportado: " + method);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}