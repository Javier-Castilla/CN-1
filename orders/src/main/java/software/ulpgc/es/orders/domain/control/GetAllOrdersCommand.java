package software.ulpgc.es.orders.domain.control;

import software.ulpgc.es.orders.domain.io.repository.OrderRepository;
import software.ulpgc.es.orders.domain.model.Order;

import java.util.List;

public class GetAllOrdersCommand implements Command {
    private final Output output;
    private final OrderRepository orderRepository;

    public GetAllOrdersCommand(Output output, OrderRepository orderRepository) {
        this.output = output;
        this.orderRepository = orderRepository;
    }

    @Override
    public void execute() {
        this.output.result(this.orderRepository.getAllOrders());
    }

    public interface Output {
        void result(List<Order> orders);
    }
}
