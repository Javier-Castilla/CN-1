package software.ulpgc.es.orders.app.io.customer;

import com.google.gson.Gson;
import software.ulpgc.es.orders.domain.io.customer.CustomerDeserializer;
import software.ulpgc.es.orders.domain.pojos.CustomersGetResponse;

public class CustomersCustomerDeserializer implements CustomerDeserializer {
    @Override
    public Object deserialize(String string) {
        return new Gson().fromJson(string, CustomersGetResponse.class);
    }
}
