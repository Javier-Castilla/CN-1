package software.ulpgc.es.orders.adapters;

import software.ulpgc.es.orders.domain.model.Customer;
import software.ulpgc.es.orders.domain.pojos.CustomersGetResponse;

public class JSONCustomerAdapter {
    public static Customer adapt(CustomersGetResponse response) {
        return new Customer(response.id(), response.name(), response.name());
    }
}
