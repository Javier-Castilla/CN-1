package software.ulpgc.es.orders.app.io.customer;

import software.ulpgc.es.orders.domain.io.customer.CustomerReader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class CustomersCustomerReader implements CustomerReader {
    @Override
    public String read(String path) {
        try (InputStream is = new URL(path).openStream()) {
            return new String(is.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
