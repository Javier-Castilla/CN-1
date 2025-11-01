package software.ulpgc.es.orders.app;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.ulpgc.es.orders.app.io.repository.PostgreSQLOrderRepository;
import software.ulpgc.es.orders.domain.control.*;
import software.ulpgc.es.orders.domain.io.repository.OrderRepository;
import software.ulpgc.es.orders.domain.model.ISBN;
import software.ulpgc.es.orders.domain.model.Order;
import software.ulpgc.es.orders.domain.model.OrderItem;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainLambda implements RequestHandler<Map<String,Object>, Object> {
    private final ObjectMapper mapper = new ObjectMapper();
    private final OrderRepository orderRepository;

    public MainLambda() {
        try {
            String type = System.getenv("DB_TYPE");
            String host = System.getenv("DB_HOST");
            String port = System.getenv("DB_PORT");
            String dbName = System.getenv("DB_NAME");
            String user = System.getenv("DB_USER");
            String pass = System.getenv("DB_PASS");
            String url = String.format("jdbc:%s://%s:%s/%s", type, host, port, dbName);
            this.orderRepository = new PostgreSQLOrderRepository(url, user, pass);
        } catch (Exception e) {
            throw new RuntimeException("Error al inicializar OrderRepository", e);
        }
    }

    @Override
    public Object handleRequest(Map<String,Object> input, Context context) {
        try {
            String method = (String) input.get("httpMethod");
            String path = (String) input.get("path");
            String body = (String) input.get("body");
            Map<String,Object> data = body != null ? mapper.readValue(body, Map.class) : Map.of();
            Order[] resultHandler = new Order[1];
            switch (method) {
                case "POST":
                    Order orderToSave = getOrderFromBody(data);
                    new CreateOrderCommand(() -> orderToSave, result -> resultHandler[0] = result, this.orderRepository).execute();
                    return resultHandler[0];
                case "GET":
                    String orderId = ((Map<String,String>) input.get("queryStringParameters")).get("orderId");
                    String customerId = ((Map<String,String>) input.get("queryStringParameters")).get("customerId");
                    if (customerId != null) {
                        if (orderId != null) {
                            new GetCustomerOrderCommand(new GetCustomerOrderCommand.Input() {

                                @Override
                                public int customerId() {
                                    return Integer.parseInt(customerId);
                                }

                                @Override
                                public int orderId() {
                                    return Integer.parseInt(orderId);
                                }
                            }, result -> resultHandler[0] = result, orderRepository).execute();
                            return resultHandler[0];
                        } else {
                            List<Order> orders = new ArrayList<>();
                            new GetCustomerOrdersCommand(() -> Integer.parseInt(customerId), orders::addAll, orderRepository).execute();
                            return orders;
                        }
                    } else {
                        List<Order> orders = new ArrayList<>();
                        new GetAllOrdersCommand(orders::addAll, orderRepository).execute();
                        return orders;
                    }
                case "PUT":
                    Order orderToUpdate = getOrderFromBody(data);
                    new UpdateOrderCommand(() -> orderToUpdate, result -> resultHandler[0] = result, this.orderRepository).execute();
                    return resultHandler[0];
                case "DELETE":
                    Map<String, String> queryParams = (Map<String, String>) input.get("queryStringParameters");
                    int deleteId = Integer.parseInt(queryParams.get("id"));
                    new DeleteOrderCommand(() -> deleteId, result -> resultHandler[0] = result, this.orderRepository).execute();
                    return resultHandler[0];
                default:
                    throw new IllegalArgumentException("Método no soportado: " + method);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Order getOrderFromBody(Map<String, Object> data) {
        int updateId = (int) data.get("id");
        int customerId = (int) data.get("customerId");
        LocalDateTime date = LocalDateTime.parse((String) data.get("date"));
        List<Map<String, Object>> itemsData = (List<Map<String, Object>>) data.get("items");
        List<OrderItem> items = itemsData.stream()
                .map(item -> {
                    Map<String, Object> isbnMap = (Map<String, Object>) item.get("isbn");
                    String isbnValue = (String) isbnMap.get("value");
                    int quantity = (int) item.get("quantity");
                    return new OrderItem(new ISBN(isbnValue), quantity);
                })
                .toList();
        return new Order(updateId, customerId, date, items);
    }
}