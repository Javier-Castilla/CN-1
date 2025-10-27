package software.ulpgc.es.orders.app.io.customer;

import software.ulpgc.es.orders.domain.io.customer.CustomerDeserializer;
import software.ulpgc.es.orders.domain.io.customer.CustomerReader;

public class CustomerLoader {
    private final CustomerReader reader;
    private final CustomerDeserializer deserializer;

    public CustomerLoader(CustomerReader reader, CustomerDeserializer deserializer) {
        this.reader = reader;
        this.deserializer = deserializer;
    }

    public Object load(String url) {
        return this.deserializer.deserialize(this.reader.read(url));
    }
}
