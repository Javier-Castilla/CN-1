package software.ulpgc.es.practicacn1.domain.model;

import java.time.LocalDateTime;
import java.util.List;

public record Order(int id, Customer customer, LocalDateTime date, List<OrderItem> items) {}
