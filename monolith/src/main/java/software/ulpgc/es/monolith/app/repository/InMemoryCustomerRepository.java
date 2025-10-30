package software.ulpgc.es.monolith.app.repository;

import software.ulpgc.es.monolith.domain.model.Customer;
import software.ulpgc.es.monolith.domain.repository.CustomerRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryCustomerRepository implements CustomerRepository {
    private final Map<Integer, Customer> map;

    public InMemoryCustomerRepository() {
        this.map = new HashMap<>();
    }

    @Override
    public Customer getCustomer(int id) {
        return this.map.get(id);
    }

    @Override
    public List<Customer> getAllCustomers() {
        return this.map.values().stream().toList();
    }

    @Override
    public Customer saveCustomer(Customer customer) {
        return this.map.put(customer.id(), customer);
    }

    @Override
    public Customer updateCustomer(Customer customer) {
        return this.map.put(customer.id(), customer);
    }

    @Override
    public Customer deleteCustomer(int id) {
        return this.map.remove(id);
    }
}
