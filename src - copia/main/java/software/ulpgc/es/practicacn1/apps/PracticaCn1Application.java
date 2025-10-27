package software.ulpgc.es.practicacn1.apps;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import software.ulpgc.es.practicacn1.repository.PostgreSQLBookRepository;
import software.ulpgc.es.practicacn1.repository.PostgreSQLCustomerRepository;
import software.ulpgc.es.practicacn1.repository.PostgreSQLOrderRepository;
import software.ulpgc.es.practicacn1.domain.control.CommandFactory;
import software.ulpgc.es.practicacn1.domain.repository.BookRepository;
import software.ulpgc.es.practicacn1.domain.repository.CustomerRepository;
import software.ulpgc.es.practicacn1.domain.repository.OrderRepository;

@SpringBootApplication
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
    public BookRepository bookRepository() {
        return new PostgreSQLBookRepository(buildJdbcUrl(), dbUsername, dbPassword);
    }

    @Bean
    public CustomerRepository customerRepository() {
        return new PostgreSQLCustomerRepository(buildJdbcUrl(), dbUsername, dbPassword);
    }

    @Bean
    public OrderRepository orderRepository(BookRepository bookRepository, CustomerRepository customerRepository) {
        return new PostgreSQLOrderRepository(buildJdbcUrl(), dbUsername, dbPassword, customerRepository, bookRepository);
    }

    @Bean
    public CommandFactory commandFactory() {
        return new CommandFactory();
    }
}
