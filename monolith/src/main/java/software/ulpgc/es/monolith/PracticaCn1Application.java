package software.ulpgc.es.monolith;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import software.ulpgc.es.books.app.repository.PostgreSQLBookRepository;
import software.ulpgc.es.books.domain.control.CommandFactory;
import software.ulpgc.es.books.domain.repository.BookRepository;
import software.ulpgc.es.customers.app.repository.PostgreSQLCustomerRepository;
import software.ulpgc.es.customers.domain.repository.CustomerRepository;
import software.ulpgc.es.orders.app.io.repository.PostgreSQLOrderRepository;
import software.ulpgc.es.orders.domain.repository.OrderRepository;

@SpringBootApplication(scanBasePackages = {"software.ulpgc.es.monolith", "software.ulpgc.es.books", "software.ulpgc.es.customers", "software.ulpgc.es.orders"})
@Profile("monolith")
public class PracticaCn1Application {
    @Value("${DB_TYPE}")
    private String dbType;

    @Value("${DB_HOST}")
    private String dbHost;

    @Value("${DB_PORT}")
    private String dbPort;

    @Value("${DB_NAME}")
    private String dbName;

    @Value("${DB_USERNAME}")
    private String dbUsername;

    @Value("${DB_PASSWORD}")
    private String dbPassword;

    public static void main(String[] args) {
        SpringApplication.run(PracticaCn1Application.class, args);
    }

    private String buildJdbcUrl() {
        return String.format("jdbc:%s://%s:%s/%s", dbType, dbHost, dbPort, dbName);
    }

    @Bean
    public OrderRepository orderRepository() {
        return new PostgreSQLOrderRepository(buildJdbcUrl(), dbUsername, dbPassword);
    }

    @Bean
    public CustomerRepository customerRepository() {
        return new PostgreSQLCustomerRepository(buildJdbcUrl(), dbUsername, dbPassword);
    }

    @Bean
    public BookRepository bookRepository() {
        return new PostgreSQLBookRepository(buildJdbcUrl(), dbUsername, dbPassword);
    }

    @Bean
    public CommandFactory commandFactory() {
        return new CommandFactory();
    }
}
