package software.ulpgc.es.orders.domain.model;

import java.time.LocalDateTime;
import java.util.List;

public record Order(int id, Customer customer, LocalDateTime date, List<OrderItem> items) {}
