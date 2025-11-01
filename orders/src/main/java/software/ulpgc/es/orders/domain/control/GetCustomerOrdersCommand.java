package software.ulpgc.es.orders.domain.control;

import software.ulpgc.es.orders.domain.io.repository.OrderRepository;
import software.ulpgc.es.orders.domain.model.Order;

import java.util.List;

public class GetCustomerOrdersCommand implements Command {
    private final Input input;
    private final Output output;
    private final OrderRepository orderRepository;

    public GetCustomerOrdersCommand(Input input, Output output, OrderRepository orderRepository) {
        this.input = input;
        this.output = output;
        this.orderRepository = orderRepository;
    }

    @Override
    public void execute() {
        this.output.result(this.orderRepository.getOrdersByCustomer(this.input.id()));
    }

    public interface Input {
        int id();
    }

    public interface Output {
        void result(List<Order> orders);
    }
}
