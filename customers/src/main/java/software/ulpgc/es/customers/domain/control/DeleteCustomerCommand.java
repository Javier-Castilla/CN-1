package software.ulpgc.es.customers.domain.control;

import software.ulpgc.es.customers.domain.io.repository.CustomerRepository;
import software.ulpgc.es.customers.domain.model.Customer;

public class DeleteCustomerCommand implements Command {
    private final Input input;
    private final Output output;
    private final CustomerRepository customerRepository;

    public DeleteCustomerCommand(Input input, Output output, CustomerRepository customerRepository) {
        this.input = input;
        this.output = output;
        this.customerRepository = customerRepository;
    }

    @Override
    public void execute() {
        this.output.result(this.customerRepository.deleteCustomer(this.input.id()));
    }

    public interface Input {
        int id();
    }

    public interface Output {
        void result(Customer customer);
    }
}
