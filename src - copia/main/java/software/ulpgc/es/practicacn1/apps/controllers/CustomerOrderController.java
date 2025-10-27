package software.ulpgc.es.practicacn1.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.ulpgc.es.practicacn1.domain.model.Order;
import software.ulpgc.es.practicacn1.domain.repository.OrderRepository;

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
        if (order != null && order.customer().id() == customerId) {
            return ResponseEntity.ok(order);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@PathVariable int customerId, @RequestBody Order order) {
        Order newOrder = new Order(0, order.customer(), order.date(), order.items());
        System.out.println(newOrder);
        Order savedOrder = orderRepository.saveOrder(newOrder);
        System.out.println(savedOrder);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedOrder);
    }
}
