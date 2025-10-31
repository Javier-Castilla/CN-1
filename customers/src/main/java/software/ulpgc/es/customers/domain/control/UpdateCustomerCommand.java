package software.ulpgc.es.customers.domain.control;

import software.ulpgc.es.customers.domain.io.repository.CustomerRepository;
import software.ulpgc.es.customers.domain.model.Customer;

public class UpdateCustomerCommand implements Command {
    private final Input input;
    private final Output output;
    private final CustomerRepository customerRepository;

    public UpdateCustomerCommand(Input input, Output output, CustomerRepository customerRepository) {
        this.input = input;
        this.output = output;
        this.customerRepository = customerRepository;
    }

    @Override
    public void execute() {
        this.output.result(this.customerRepository.updateCustomer(this.input.customer()));
    }

    public interface Input {
        Customer customer();
    }

    public interface Output {
        void result(Customer customer);
    }
}
