package software.ulpgc.es.orders.domain.model;

import java.time.LocalDateTime;
import java.util.List;

public record Order(int id, int customerId, LocalDateTime date, List<OrderItem> items) {}
