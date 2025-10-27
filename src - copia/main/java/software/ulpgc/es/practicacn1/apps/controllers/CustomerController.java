package software.ulpgc.es.practicacn1.controllers;

import org.springframework.web.bind.annotation.*;
import software.ulpgc.es.practicacn1.domain.model.Customer;
import software.ulpgc.es.practicacn1.domain.model.Order;
import software.ulpgc.es.practicacn1.domain.repository.CustomerRepository;
import software.ulpgc.es.practicacn1.domain.repository.OrderRepository;

import java.util.List;

@RestController
@RequestMapping("/customers")
public class CustomerController {
    private final CustomerRepository customerRepository;

    public CustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @GetMapping
    public List<Customer> getAllCustomers() {
        return this.customerRepository.getAllCustomers();
    }

    @GetMapping("/{id}")
    public Customer getCustomer(@PathVariable int id) {
        return this.customerRepository.getCustomer(id);
    }

    @PostMapping
    public Customer createCustomer(@RequestBody Customer customer) {
        return this.customerRepository.saveCustomer(customer);
    }

    @PutMapping
    public Customer updateCustomer(@RequestBody Customer customer) {
        return this.customerRepository.updateCustomer(customer);
    }

    @DeleteMapping("/{id}")
    public Customer deleteCustomer(@PathVariable int id) {
        return this.customerRepository.deleteCustomer(id);
    }
}
