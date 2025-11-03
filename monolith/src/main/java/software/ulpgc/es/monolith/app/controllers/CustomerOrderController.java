package software.ulpgc.es.monolith.app.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.ulpgc.es.monolith.domain.model.Order;
import software.ulpgc.es.monolith.domain.model.OrderItem;
import software.ulpgc.es.monolith.domain.control.CommandFactory;
import software.ulpgc.es.monolith.domain.control.orders.*;
import software.ulpgc.es.monolith.domain.io.repository.exceptions.books.InsufficientStockException;
import software.ulpgc.es.monolith.domain.io.repository.exceptions.orders.OrderNotFoundException;
import software.ulpgc.es.monolith.domain.io.repository.exceptions.orders.OrdersDatabaseException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/customers/{customerId}/orders")
public class CustomerOrderController {

    private final CommandFactory commandFactory;

    public CustomerOrderController(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    @GetMapping
    public ResponseEntity<?> getOrdersByCustomer(@PathVariable int customerId) {
        try {
            final List<Order>[] resultHolder = new List[1];
            GetCustomerOrdersCommand.Input input = () -> customerId;
            GetCustomerOrdersCommand.Output output = result -> resultHolder[0] = result;
            commandFactory.with(input, output)
                    .build("getCustomerOrders")
                    .execute();
            return ResponseEntity.ok(resultHolder[0]);
        } catch (OrdersDatabaseException e) {
            return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving customer orders");
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrder(@PathVariable int customerId, @PathVariable int orderId) {
        try {
            final Order[] resultHolder = new Order[1];
            GetCustomerOrderCommand.Input input = new GetCustomerOrderCommand.Input() {
                @Override public int customerId() { return customerId; }
                @Override public int orderId() { return orderId; }
            };
            GetCustomerOrderCommand.Output output = result -> resultHolder[0] = result;
            commandFactory.with(input, output)
                    .build("getCustomerOrder")
                    .execute();

            if (resultHolder[0] != null) {
                return ResponseEntity.ok(resultHolder[0]);
            } else {
                return buildError(HttpStatus.NOT_FOUND, "Order not found");
            }
        } catch (OrderNotFoundException e) {
            return buildError(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (OrdersDatabaseException e) {
            return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving order");
        }
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@PathVariable int customerId, @RequestBody Order order) {
        try {
            final Order[] resultHolder = new Order[1];
            CreateOrderCommand.Input input = () -> {
                List<OrderItem> items = order.items().stream()
                        .map(item -> new OrderItem(item.isbn(), item.quantity()))
                        .toList();
                return new Order(0, customerId, LocalDateTime.now(), items);
            };
            CreateOrderCommand.Output output = result -> resultHolder[0] = result;
            commandFactory.with(input, output)
                    .build("createOrder")
                    .execute();

            return ResponseEntity.status(HttpStatus.CREATED).body(resultHolder[0]);

        } catch (InsufficientStockException e) {
            return buildError(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (OrdersDatabaseException e) {
            return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating order");
        }
    }

    private ResponseEntity<ErrorResponse> buildError(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(status.value(), message));
    }

    private record ErrorResponse(int status, String message) {}
}
