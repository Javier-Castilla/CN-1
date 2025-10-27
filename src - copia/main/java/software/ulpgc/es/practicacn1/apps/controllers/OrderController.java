package software.ulpgc.es.practicacn1.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.ulpgc.es.practicacn1.domain.model.Order;
import software.ulpgc.es.practicacn1.domain.repository.OrderRepository;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderRepository orderRepository;

    public OrderController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrder(@PathVariable int orderId) {
        Order order = orderRepository.getOrder(orderId);
        if (order != null) {
            return ResponseEntity.ok(order);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<Order> updateOrder(@PathVariable int orderId, @RequestBody Order order) {
        Order updatedOrder = orderRepository.updateOrder(new Order(orderId, order.customer(), order.date(), order.items()));
        if (updatedOrder != null) {
            return ResponseEntity.ok(updatedOrder);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Order> cancelOrder(@PathVariable int orderId) {
        Order cancelled = orderRepository.cancelOrder(orderId);
        if (cancelled != null) {
            return ResponseEntity.ok(cancelled);
        }
        return ResponseEntity.notFound().build();
    }
}
