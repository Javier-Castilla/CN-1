package software.ulpgc.es.monolith.app.controllers;

import org.springframework.web.bind.annotation.*;
import software.ulpgc.es.monolith.app.Main;
import software.ulpgc.es.monolith.domain.control.CommandFactory;
import software.ulpgc.es.monolith.domain.model.Customer;
import software.ulpgc.es.monolith.domain.io.repository.CustomerRepository;
import software.ulpgc.es.monolith.domain.control.customers.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
public class CustomerController {
    private final CommandFactory commandFactory;

    public CustomerController(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    @GetMapping
    public List<Customer> getAllCustomers() {
        final List<Customer>[] resultHolder = new List[1];
        GetAllCustomersCommand.Output output = result -> resultHolder[0] = result;
        commandFactory.with(Main.NoInput.INSTANCE, output).build("getCustomers").execute();
        return resultHolder[0];
    }

    @GetMapping("/{id}")
    public Customer getCustomer(@PathVariable int id) {
        final Customer[] resultHolder = new Customer[1];
        GetCustomerCommand.Input input = () -> id;
        GetCustomerCommand.Output output = result -> resultHolder[0] = result;
        commandFactory.with(input, output).build("getCustomer").execute();
        return resultHolder[0];
    }

    @PostMapping
    public Customer createCustomer(@RequestBody Customer customer) {
        final Customer[] resultHolder = new Customer[1];
        CreateCustomerCommand.Input input = () -> customer;
        CreateCustomerCommand.Output output = result -> resultHolder[0] = result;
        commandFactory.with(input, output).build("createCustomer").execute();
        return resultHolder[0];
    }

    @PutMapping
    public Customer updateCustomer(@RequestBody Customer customer) {
        final Customer[] resultHolder = new Customer[1];
        UpdateCustomerCommand.Input input = () -> customer;
        UpdateCustomerCommand.Output output = result -> resultHolder[0] = result;
        commandFactory.with(input, output).build("updateCustomer").execute();
        return resultHolder[0];
    }

    @DeleteMapping("/{id}")
    public Customer deleteCustomer(@PathVariable int id) {
        final Customer[] resultHolder = new Customer[1];
        DeleteCustomerCommand.Input input = () -> id;
        DeleteCustomerCommand.Output output = result -> resultHolder[0] = result;
        commandFactory.with(input, output).build("deleteCustomer").execute();
        return resultHolder[0];
    }
}
