package software.ulpgc.es.practicacn1;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import software.ulpgc.es.books.app.repository.PostgreSQLBookRepository;
import software.ulpgc.es.books.domain.control.CommandFactory;
import software.ulpgc.es.books.domain.repository.BookRepository;
import software.ulpgc.es.customers.app.repository.PostgreSQLCustomerRepository;
import software.ulpgc.es.customers.domain.repository.CustomerRepository;
import software.ulpgc.es.orders.app.io.books.BookLoader;
import software.ulpgc.es.orders.app.io.books.BooksBookDeserializer;
import software.ulpgc.es.orders.app.io.books.BooksBookReader;
import software.ulpgc.es.orders.app.io.customer.CustomerLoader;
import software.ulpgc.es.orders.app.io.customer.CustomersCustomerDeserializer;
import software.ulpgc.es.orders.app.io.customer.CustomersCustomerReader;
import software.ulpgc.es.orders.app.repository.PostgreSQLOrderRepository;
import software.ulpgc.es.orders.domain.repository.OrderRepository;

@SpringBootApplication(scanBasePackages = "software.ulpgc.es.practicacn1")
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

    @Value("${CUSTOMERS_URL}")
    private String customersURL;

    @Value("${BOOKS_URL}")
    private String booksURL;

    public static void main(String[] args) {
        SpringApplication.run(PracticaCn1Application.class, args);
    }

    private String buildJdbcUrl() {
        return String.format("jdbc:%s://%s:%s/%s", dbType, dbHost, dbPort, dbName);
    }

    @Bean
    public BookRepository bookRepository() {
        return new PostgreSQLBookRepository(buildJdbcUrl(), dbUsername, dbPassword);
    }

    @Bean
    public CustomerRepository customerRepository() {
        return new PostgreSQLCustomerRepository(buildJdbcUrl(), dbUsername, dbPassword);
    }

    @Bean
    public OrderRepository orderRepository() {
        return new PostgreSQLOrderRepository(buildJdbcUrl(), dbUsername, dbPassword, booksURL, customersURL, createBookLoader(), createCustomerLoader());
    }

    private CustomerLoader createCustomerLoader() {
        return new CustomerLoader(new CustomersCustomerReader(), new CustomersCustomerDeserializer());
    }

    private BookLoader createBookLoader() {
        return new BookLoader(new BooksBookReader(), new BooksBookDeserializer());
    }

    @Bean
    public CommandFactory commandFactory() {
        return new CommandFactory();
    }
}
