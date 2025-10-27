package software.ulpgc.es.practicacn1.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.ulpgc.es.books.domain.model.Book;
import software.ulpgc.es.books.domain.repository.BookRepository;
import software.ulpgc.es.customers.domain.model.Customer;
import software.ulpgc.es.customers.domain.repository.CustomerRepository;
import software.ulpgc.es.orders.domain.model.Order;
import software.ulpgc.es.orders.domain.model.OrderItem;
import software.ulpgc.es.orders.domain.repository.OrderRepository;
import software.ulpgc.es.practicacn1.dto.CreateOrderDTO;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/customers/{customerId}/orders")
public class CustomerOrderController {
    private final OrderRepository orderRepository;
    private final BookRepository bookRepository;
    private final CustomerRepository customerRepository;

    public CustomerOrderController(OrderRepository orderRepository, BookRepository bookRepository, CustomerRepository customerRepository) {
        this.orderRepository = orderRepository;
        this.bookRepository = bookRepository;
        this.customerRepository = customerRepository;
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
    public ResponseEntity<Order> createOrder(
            @PathVariable int customerId,
            @RequestBody CreateOrderDTO orderDTO) {

        Customer customer = customerRepository.getCustomer(customerId);

        System.out.println(orderDTO);

        List<OrderItem> items = orderDTO.items().stream().map(itemDTO -> {
            Book book = bookRepository.getBook(itemDTO.bookIsbn());
            return new OrderItem(book, itemDTO.quantity());
        }).toList();

        Order newOrder = new Order(0, customer, LocalDateTime.now(), items);

        Order savedOrder = orderRepository.saveOrder(newOrder);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedOrder);
    }

}
