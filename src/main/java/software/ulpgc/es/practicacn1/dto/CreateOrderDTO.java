package software.ulpgc.es.practicacn1.dto;

import java.util.List;

public record CreateOrderDTO(int customerId, List<OrderItemDTO> items) {}
