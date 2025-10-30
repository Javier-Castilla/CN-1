package software.ulpgc.es.monolith.domain.control.orders;

import software.ulpgc.es.monolith.domain.control.Command;
import software.ulpgc.es.monolith.domain.io.repository.OrderRepository;
import software.ulpgc.es.monolith.domain.model.Order;

public class GetCustomerOrderCommand implements Command {
    private final Input input;
    public Output output;
    public OrderRepository repository;

    public GetCustomerOrderCommand(Input input, Output output, OrderRepository repository) {
        this.input = input;
        this.output = output;
        this.repository = repository;
    }

    @Override
    public void execute() {
        Order savedOrder = this.repository.getOrder(this.input.orderId());
        this.output.result(savedOrder.customerId() == this.input.customerId() ? savedOrder : null);
    }

    public interface Input {
        int customerId();
        int orderId();
    }

    public interface Output {
        void result(Order order);
    }
}
