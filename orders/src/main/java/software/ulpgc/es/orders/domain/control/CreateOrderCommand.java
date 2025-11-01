package software.ulpgc.es.orders.domain.control;

import software.ulpgc.es.orders.domain.io.repository.OrderRepository;
import software.ulpgc.es.orders.domain.model.Order;

public class CreateOrderCommand implements Command {
    private final Input input;
    private final Output output;
    private final OrderRepository repository;

    public CreateOrderCommand(Input input, Output output, OrderRepository repository) {
        this.input = input;
        this.output = output;
        this.repository = repository;
    }

    @Override
    public void execute() {
        this.output.result(this.repository.saveOrder(this.input.order()));
    }

    public interface Input {
        Order order();
    }

    public interface Output {
        void result(Order order);
    }
}
