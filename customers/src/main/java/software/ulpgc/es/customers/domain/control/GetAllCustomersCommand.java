package software.ulpgc.es.customers.domain.control;

import software.ulpgc.es.customers.domain.io.repository.CustomerRepository;
import software.ulpgc.es.customers.domain.model.Customer;

import java.util.List;

public class GetAllCustomersCommand implements Command {
    private final Output output;
    private final CustomerRepository customerRepository;

    public GetAllCustomersCommand(Output output, CustomerRepository customerRepository) {
        this.output = output;
        this.customerRepository = customerRepository;
    }

    @Override
    public void execute() {
        this.output.result(this.customerRepository.getAllCustomers());
    }

    public interface Output {
        void result(List<Customer> customers);
    }
}
