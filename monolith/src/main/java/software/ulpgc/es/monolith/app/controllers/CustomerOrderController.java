package software.ulpgc.es.monolith.app.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.ulpgc.es.monolith.domain.model.Order;
import software.ulpgc.es.monolith.domain.model.OrderItem;
import software.ulpgc.es.monolith.domain.control.CommandFactory;
import software.ulpgc.es.monolith.domain.control.orders.*;

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
    public ResponseEntity<List<Order>> getOrdersByCustomer(@PathVariable int customerId) {
        final List<Order>[] resultHolder = new List[1];
        GetCustomerOrdersCommand.Input input = () -> customerId;
        GetCustomerOrdersCommand.Output output = result -> resultHolder[0] = result;
        commandFactory.with(input, output)
                .build("getCustomerOrders")
                .execute();
        return ResponseEntity.ok(resultHolder[0]);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrder(@PathVariable int customerId, @PathVariable int orderId) {
        final Order[] resultHolder = new Order[1];
        GetCustomerOrderCommand.Input input = new GetCustomerOrderCommand.Input() {
            @Override
            public int customerId() {
                return customerId;
            }
            @Override
            public int orderId() {
                return orderId;
            }
        };
        GetCustomerOrderCommand.Output output = result -> resultHolder[0] = result;
        commandFactory.with(input, output)
                .build("getCustomerOrder")
                .execute();
        if (resultHolder[0] != null) {
            return ResponseEntity.ok(resultHolder[0]);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@PathVariable int customerId, @RequestBody Order order) {
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
    }
}
