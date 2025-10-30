package software.ulpgc.es.monolith.domain.io.repository;

import software.ulpgc.es.monolith.domain.model.Customer;

import java.util.List;

public interface CustomerRepository {
    Customer getCustomer(int id);
    List<Customer> getAllCustomers();
    Customer saveCustomer(Customer customer);
    Customer updateCustomer(Customer customer);
    Customer deleteCustomer(int id);
}
