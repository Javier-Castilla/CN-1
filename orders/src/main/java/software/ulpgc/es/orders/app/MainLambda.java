package software.ulpgc.es.orders.app;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.ulpgc.es.orders.app.io.repository.PostgreSQLOrderRepository;
import software.ulpgc.es.orders.domain.control.*;
import software.ulpgc.es.orders.domain.io.repository.OrderRepository;
import software.ulpgc.es.orders.domain.io.repository.exceptions.*;
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
            String body = (String) input.get("body");
            Map<String,Object> data = body != null && !body.isEmpty() ? mapper.readValue(body, Map.class) : Map.of();
            Order[] resultHandler = new Order[1];
            switch (method) {
                case "POST" -> {
                    Order orderToSave = getOrderFromBodySafe(data);
                    new CreateOrderCommand(() -> orderToSave, result -> resultHandler[0] = result, this.orderRepository).execute();
                    return buildResponse(201, resultHandler[0]);
                }
                case "GET" -> {
                    Map<String,String> queryParams = (Map<String,String>) input.get("queryStringParameters");
                    String orderId = queryParams != null ? queryParams.get("orderId") : null;
                    String customerId = queryParams != null ? queryParams.get("customerId") : null;
                    if (customerId != null) {
                        if (orderId != null) {
                            int cId = Integer.parseInt(customerId);
                            int oId = Integer.parseInt(orderId);
                            new GetCustomerOrderCommand(new GetCustomerOrderCommand.Input() {
                                public int customerId() { return cId; }
                                public int orderId() { return oId; }
                            }, result -> resultHandler[0] = result, orderRepository).execute();
                            return buildResponse(200, resultHandler[0]);
                        } else {
                            List<Order> orders = new ArrayList<>();
                            new GetCustomerOrdersCommand(() -> Integer.parseInt(customerId), orders::addAll, orderRepository).execute();
                            return buildResponse(200, orders);
                        }
                    } else {
                        List<Order> orders = new ArrayList<>();
                        new GetAllOrdersCommand(orders::addAll, orderRepository).execute();
                        return buildResponse(200, orders);
                    }
                }
                case "PUT" -> {
                    Order orderToUpdate = getOrderFromBodySafe(data);
                    if (orderToUpdate.id() <= 0) throw new IllegalArgumentException("ID es obligatorio para actualizar un pedido");
                    new UpdateOrderCommand(() -> orderToUpdate, result -> resultHandler[0] = result, this.orderRepository).execute();
                    return buildResponse(200, resultHandler[0]);
                }
                case "DELETE" -> {
                    Map<String, String> queryParams = (Map<String, String>) input.get("queryStringParameters");
                    if (queryParams == null || queryParams.get("id") == null) throw new IllegalArgumentException("ID es obligatorio para eliminar un pedido");
                    int deleteId = Integer.parseInt(queryParams.get("id"));
                    new DeleteOrderCommand(() -> deleteId, result -> resultHandler[0] = result, this.orderRepository).execute();
                    return buildResponse(200, resultHandler[0]);
                }
                default -> throw new IllegalArgumentException("Método no soportado: " + method);
            }
        } catch (IllegalArgumentException e) {
            return buildError(400, e.getMessage());
        } catch (OrderNotFoundException e) {
            return buildError(404, e.getMessage());
        } catch (InsufficientStockException e) {
            return buildError(409, e.getMessage());
        } catch (OrdersDatabaseException e) {
            return buildError(500, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return buildError(500, "Error inesperado procesando la petición: " + e.getMessage());
        }
    }

    private static Order getOrderFromBodySafe(Map<String, Object> data) {
        int id = parseIntSafe(data.get("id"));
        int customerId = parseIntSafe(data.get("customerId"));
        if (customerId <= 0) throw new IllegalArgumentException("customerId es obligatorio y debe ser > 0");
        LocalDateTime date = data.get("date") != null ? LocalDateTime.parse(data.get("date").toString()) : LocalDateTime.now();
        List<Map<String, Object>> itemsData = (List<Map<String, Object>>) data.getOrDefault("items", List.of());
        List<OrderItem> items = itemsData.stream()
                .map(item -> {
                    Object isbnObj = item.get("isbn");
                    String isbnValue;
                    if (isbnObj instanceof Map) {
                        isbnValue = ((Map<?,?>) isbnObj).get("value").toString();
                    } else {
                        isbnValue = isbnObj.toString();
                    }
                    int quantity = parseIntSafe(item.get("quantity"));
                    if (quantity <= 0) throw new IllegalArgumentException("quantity debe ser > 0 para cada item");
                    return new OrderItem(new ISBN(isbnValue), quantity);
                })
                .toList();
        return new Order(id, customerId, date, items);
    }

    private static int parseIntSafe(Object obj) {
        if (obj == null) return 0;
        if (obj instanceof Number) return ((Number) obj).intValue();
        try {
            return Integer.parseInt(obj.toString());
        } catch (Exception e) {
            return 0;
        }
    }

    private Map<String, Object> buildResponse(int statusCode, Object body) {
        return Map.of(
                "statusCode", statusCode,
                "body", toJson(body),
                "headers", Map.of("Content-Type", "application/json")
        );
    }

    private Map<String, Object> buildError(int statusCode, String message) {
        ErrorResponse error = new ErrorResponse(statusCode, message);
        return Map.of(
                "statusCode", statusCode,
                "body", toJson(error),
                "headers", Map.of("Content-Type", "application/json")
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
