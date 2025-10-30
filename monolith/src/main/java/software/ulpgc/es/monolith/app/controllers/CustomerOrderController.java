package software.ulpgc.es.monolith.app.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.ulpgc.es.monolith.domain.model.Order;
import software.ulpgc.es.monolith.domain.model.OrderItem;
import software.ulpgc.es.monolith.domain.repository.OrderRepository;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/customers/{customerId}/orders")
public class CustomerOrderController {
    private final OrderRepository orderRepository;

    public CustomerOrderController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @GetMapping
    public ResponseEntity<List<Order>> getOrdersByCustomer(@PathVariable int customerId) {
        List<Order> orders = orderRepository.getOrdersByCustomer(customerId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrder(@PathVariable int customerId, @PathVariable int orderId) {
        Order order = orderRepository.getOrder(orderId);
        if (order != null && order.customerId() == customerId) {
            return ResponseEntity.ok(order);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@PathVariable int customerId, @RequestBody Order order) {
        List<OrderItem> items = order.items().stream().map(item -> new OrderItem(item.isbn(), item.quantity())).toList();
        Order savedOrder = orderRepository.saveOrder(new Order(0, customerId, LocalDateTime.now(), items));
        return ResponseEntity.status(HttpStatus.CREATED).body(savedOrder);
    }

}
