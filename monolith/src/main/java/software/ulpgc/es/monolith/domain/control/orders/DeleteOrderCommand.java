package software.ulpgc.es.monolith.domain.control.orders;

import software.ulpgc.es.monolith.domain.control.Command;
import software.ulpgc.es.monolith.domain.io.repository.OrderRepository;
import software.ulpgc.es.monolith.domain.model.Order;

public class DeleteOrderCommand implements Command {
    private final Input input;
    private final Output output;
    private final OrderRepository repository;

    public DeleteOrderCommand(Input input, Output output, OrderRepository repository) {
        this.input = input;
        this.output = output;
        this.repository = repository;
    }

    @Override
    public void execute() {
        this.output.result(this.repository.cancelOrder(this.input.id()));
    }

    public interface Input {
        int id();
    }

    public interface Output {
        void result(Order order);
    }
}
