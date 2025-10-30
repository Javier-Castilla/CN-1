package software.ulpgc.es.monolith.domain.io.repository;

import software.ulpgc.es.monolith.domain.model.Order;

import java.util.List;

public interface OrderRepository {
    Order getOrder(int id);
    List<Order> getAllOrders();
    List<Order> getOrdersByCustomer(int customerId);
    Order saveOrder(Order order);
    Order updateOrder(Order order);
    Order cancelOrder(int id);
}
