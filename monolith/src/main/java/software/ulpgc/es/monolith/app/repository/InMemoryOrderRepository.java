package software.ulpgc.es.monolith.app.repository;

import software.ulpgc.es.monolith.domain.model.Order;
import software.ulpgc.es.monolith.domain.io.repository.OrderRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryOrderRepository implements OrderRepository {
    private final Map<Integer, Order> map;

    public InMemoryOrderRepository() {
        this.map = new HashMap<>();
    }

    @Override
    public Order getOrder(int id) {
        return this.map.get(id);
    }

    @Override
    public List<Order> getAllOrders() {
        return this.map.values().stream().toList();
    }

    @Override
    public List<Order> getOrdersByCustomer(int customerId) {
        return this.map.values().stream().filter(o -> o.customerId() == customerId).toList();
    }

    @Override
    public Order saveOrder(Order order) {
        return this.map.put(order.id(), order);
    }

    @Override
    public Order updateOrder(Order order) {
        return this.map.put(order.id(), order);
    }

    @Override
    public Order cancelOrder(int id) {
        return this.map.remove(id);
    }
}
