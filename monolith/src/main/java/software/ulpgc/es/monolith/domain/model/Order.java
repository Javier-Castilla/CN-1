package software.ulpgc.es.monolith.domain.model;

import java.time.LocalDateTime;
import java.util.List;

public record Order(int id, int customerId, LocalDateTime date, List<OrderItem> items) {}
