package software.ulpgc.es.orders.app;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

public class MainLambda implements RequestHandler<Map<String, Object>, Object> {

    private final ObjectMapper mapper;
    private final OrderRepository orderRepository;

    public MainLambda() {
        try {
            this.mapper = new ObjectMapper();
            this.mapper.registerModule(new JavaTimeModule());
            this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            String type = System.getenv("DB_TYPE");
            String host = System.getenv("DB_HOST");
            String port = System.getenv("DB_PORT");
            String dbName = System.getenv("DB_NAME");
            String user = System.getenv("DB_USERNAME");
            String pass = System.getenv("DB_PASSWORD");

            String url = String.format("jdbc:%s://%s:%s/%s", type, host, port, dbName);
            this.orderRepository = new PostgreSQLOrderRepository(url, user, pass);

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize OrderRepository", e);
        }
    }

    @Override
    public Object handleRequest(Map<String, Object> input, Context context) {
        try {
            String method = (String) input.get("httpMethod");
            String body = (String) input.get("body");

            @SuppressWarnings("unchecked")
            Map<String, Object> data = body != null && !body.isEmpty()
                    ? mapper.readValue(body, Map.class)
                    : Map.of();

            @SuppressWarnings("unchecked")
            Map<String, String> pathParams = (Map<String, String>) input.get("pathParameters");

            return switch (method) {
                case "POST" -> handlePost(pathParams, data);
                case "GET" -> handleGet(pathParams);
                case "PUT" -> handlePut(pathParams, data);
                case "DELETE" -> handleDelete(pathParams);
                case "OPTIONS" -> buildCorsPreflightResponse();
                default -> buildError(400, "Unsupported method: " + method);
            };

        } catch (IllegalArgumentException e) {
            return buildError(400, e.getMessage());
        } catch (OrderNotFoundException e) {
            return buildError(404, e.getMessage());
        } catch (InsufficientStockException e) {
            return buildError(409, e.getMessage());
        } catch (OrdersDatabaseException e) {
            return buildError(500, e.getMessage());
        } catch (Exception e) {
            context.getLogger().log("ERROR: " + e.getMessage());
            e.printStackTrace();
            return buildError(500, "Unexpected error processing request: " + e.getMessage());
        }
    }

    private Object handlePost(Map<String, String> pathParams, Map<String, Object> data) {
        final Order[] resultHolder = new Order[1];
        Order orderToSave = getOrderFromBodySafe(data);
        new CreateOrderCommand(() -> orderToSave, result -> resultHolder[0] = result, orderRepository).execute();
        return buildResponse(201, resultHolder[0]);
    }

    private Object handleGet(Map<String, String> pathParams) {
        if (pathParams != null && pathParams.containsKey("customerId")) {
            int customerId = Integer.parseInt(pathParams.get("customerId"));

            if (pathParams.containsKey("orderId")) {
                final Order[] resultHolder = new Order[1];
                int orderId = Integer.parseInt(pathParams.get("orderId"));
                final int finalCustomerId = customerId;
                final int finalOrderId = orderId;
                new GetCustomerOrderCommand(new GetCustomerOrderCommand.Input() {
                    public int customerId() { return finalCustomerId; }
                    public int orderId() { return finalOrderId; }
                }, result -> resultHolder[0] = result, orderRepository).execute();
                return buildResponse(200, resultHolder[0] != null ? resultHolder[0] : new ArrayList<>());
            } else {
                List<Order> orders = new ArrayList<>();
                final int finalCustomerId = customerId;
                new GetCustomerOrdersCommand(() -> finalCustomerId, orders::addAll, orderRepository).execute();
                return buildResponse(200, orders);
            }
        } else {
            List<Order> orders = new ArrayList<>();
            new GetAllOrdersCommand(orders::addAll, orderRepository).execute();
            return buildResponse(200, orders);
        }
    }

    private Object handlePut(Map<String, String> pathParams, Map<String, Object> data) {
        if (pathParams == null || !pathParams.containsKey("orderId")) {
            throw new IllegalArgumentException("Order ID is required to update an order");
        }

        final Order[] resultHolder = new Order[1];
        int orderId = Integer.parseInt(pathParams.get("orderId"));

        int customerId = parseIntSafe(data.get("customerId"));
        if (customerId <= 0) throw new IllegalArgumentException("customerId is required and must be > 0");

        LocalDateTime date = data.get("date") != null
                ? LocalDateTime.parse(data.get("date").toString())
                : LocalDateTime.now();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> itemsData = (List<Map<String, Object>>) data.getOrDefault("items", List.of());
        List<OrderItem> items = itemsData.stream()
                .map(item -> {
                    Object isbnObj = item.get("isbn");
                    String isbnValue;
                    if (isbnObj instanceof Map)
                        isbnValue = ((Map<?, ?>) isbnObj).get("value").toString();
                    else
                        isbnValue = isbnObj.toString();
                    int quantity = parseIntSafe(item.get("quantity"));
                    if (quantity <= 0) throw new IllegalArgumentException("quantity must be > 0 for each item");
                    return new OrderItem(new ISBN(isbnValue), quantity);
                })
                .toList();

        Order orderToUpdate = new Order(orderId, customerId, date, items);
        new UpdateOrderCommand(() -> orderToUpdate, result -> resultHolder[0] = result, orderRepository).execute();
        return buildResponse(200, resultHolder[0]);
    }

    private Object handleDelete(Map<String, String> pathParams) {
        if (pathParams == null || !pathParams.containsKey("orderId")) {
            throw new IllegalArgumentException("Order ID is required to delete an order");
        }

        final Order[] resultHolder = new Order[1];
        int orderId = Integer.parseInt(pathParams.get("orderId"));
        final int finalOrderId = orderId;
        new DeleteOrderCommand(() -> finalOrderId, result -> resultHolder[0] = result, orderRepository).execute();
        return buildResponse(200, resultHolder[0]);
    }

    private static Order getOrderFromBodySafe(Map<String, Object> data) {
        int id = parseIntSafe(data.get("id"));
        int customerId = parseIntSafe(data.get("customerId"));
        if (customerId <= 0) throw new IllegalArgumentException("customerId is required and must be > 0");

        LocalDateTime date = data.get("date") != null
                ? LocalDateTime.parse(data.get("date").toString())
                : LocalDateTime.now();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> itemsData = (List<Map<String, Object>>) data.getOrDefault("items", List.of());
        List<OrderItem> items = itemsData.stream()
                .map(item -> {
                    Object isbnObj = item.get("isbn");
                    String isbnValue;
                    if (isbnObj instanceof Map)
                        isbnValue = ((Map<?, ?>) isbnObj).get("value").toString();
                    else
                        isbnValue = isbnObj.toString();
                    int quantity = parseIntSafe(item.get("quantity"));
                    if (quantity <= 0) throw new IllegalArgumentException("quantity must be > 0 for each item");
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
            e.printStackTrace();
            return String.format("{\"error\":\"Error serializing response\",\"details\":\"%s\",\"class\":\"%s\"}",
                    e.getMessage(), obj.getClass().getName());
        }
    }

    private record ErrorResponse(int status, String message) {}
}
