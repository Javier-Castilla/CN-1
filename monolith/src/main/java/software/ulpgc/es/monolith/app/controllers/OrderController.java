package software.ulpgc.es.monolith.app.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.ulpgc.es.monolith.app.Main;
import software.ulpgc.es.monolith.domain.control.CommandFactory;
import software.ulpgc.es.monolith.domain.control.orders.*;
import software.ulpgc.es.monolith.domain.model.Order;
import software.ulpgc.es.monolith.domain.io.repository.exceptions.books.InsufficientStockException;
import software.ulpgc.es.monolith.domain.io.repository.exceptions.orders.OrderNotFoundException;
import software.ulpgc.es.monolith.domain.io.repository.exceptions.orders.OrdersDatabaseException;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final CommandFactory commandFactory;

    public OrderController(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    @GetMapping
    public ResponseEntity<?> getAllOrders() {
        try {
            final List<Order>[] resultHolder = new List[1];
            GetAllOrdersCommand.Output output = result -> resultHolder[0] = result;
            commandFactory.with(Main.NoInput.INSTANCE, output)
                    .build("getOrders")
                    .execute();
            return ResponseEntity.ok(resultHolder[0]);
        } catch (OrdersDatabaseException e) {
            return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving all orders");
        }
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<?> updateOrder(@PathVariable int orderId, @RequestBody Order order) {
        try {
            final Order[] resultHolder = new Order[1];
            UpdateOrderCommand.Input input = () -> new Order(orderId, order.customerId(), order.date(), order.items());
            UpdateOrderCommand.Output output = result -> resultHolder[0] = result;
            commandFactory.with(input, output)
                    .build("updateOrder")
                    .execute();

            if (resultHolder[0] != null) {
                return ResponseEntity.ok(resultHolder[0]);
            } else {
                return buildError(HttpStatus.NOT_FOUND, "Order not found");
            }
        } catch (InsufficientStockException e) {
            return buildError(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (OrderNotFoundException e) {
            return buildError(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (OrdersDatabaseException e) {
            return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating order");
        }
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<?> cancelOrder(@PathVariable int orderId) {
        try {
            final Order[] resultHolder = new Order[1];
            DeleteOrderCommand.Input input = () -> orderId;
            DeleteOrderCommand.Output output = result -> resultHolder[0] = result;
            commandFactory.with(input, output)
                    .build("cancelOrder")
                    .execute();

            if (resultHolder[0] != null) {
                return ResponseEntity.ok(resultHolder[0]);
            } else {
                return buildError(HttpStatus.NOT_FOUND, "Order not found");
            }
        } catch (OrderNotFoundException e) {
            return buildError(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (OrdersDatabaseException e) {
            return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Error canceling order");
        }
    }

    private ResponseEntity<ErrorResponse> buildError(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(status.value(), message));
    }

    private record ErrorResponse(int status, String message) {}
}
