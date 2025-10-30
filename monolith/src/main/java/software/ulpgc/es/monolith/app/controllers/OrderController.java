package software.ulpgc.es.monolith.app.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.ulpgc.es.monolith.app.Main;
import software.ulpgc.es.monolith.domain.model.Order;
import software.ulpgc.es.monolith.domain.control.CommandFactory;
import software.ulpgc.es.monolith.domain.control.orders.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final CommandFactory commandFactory;

    public OrderController(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    @GetMapping
    public List<Order> getAllOrders() {
        final List<Order>[] resultHolder = new List[1];
        GetAllOrdersCommand.Output output = result -> resultHolder[0] = result;
        commandFactory.with(Main.NoInput.INSTANCE, output)
                .build("getOrders")
                .execute();
        return resultHolder[0];
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<Order> updateOrder(@PathVariable int orderId, @RequestBody Order order) {
        final Order[] resultHolder = new Order[1];
        UpdateOrderCommand.Input input = () -> new Order(orderId, order.customerId(), order.date(), order.items());
        UpdateOrderCommand.Output output = result -> resultHolder[0] = result;
        commandFactory.with(input, output)
                .build("updateOrder")
                .execute();
        if (resultHolder[0] != null) {
            return ResponseEntity.ok(resultHolder[0]);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Order> cancelOrder(@PathVariable int orderId) {
        final Order[] resultHolder = new Order[1];
        DeleteOrderCommand.Input input = () -> orderId;
        DeleteOrderCommand.Output output = result -> resultHolder[0] = result;
        commandFactory.with(input, output)
                .build("cancelOrder")
                .execute();
        if (resultHolder[0] != null) {
            return ResponseEntity.ok(resultHolder[0]);
        }
        return ResponseEntity.notFound().build();
    }
}
