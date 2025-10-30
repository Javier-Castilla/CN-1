package software.ulpgc.es.monolith.app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import software.ulpgc.es.monolith.app.repository.*;
import software.ulpgc.es.monolith.domain.repository.BookRepository;
import software.ulpgc.es.monolith.domain.repository.CustomerRepository;
import software.ulpgc.es.monolith.domain.repository.OrderRepository;

@SpringBootApplication(scanBasePackages = "software.ulpgc.es.monolith")
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
        // return new PostgreSQLOrderRepository(buildJdbcUrl(), dbUsername, dbPassword);
        return new InMemoryOrderRepository();
    }

    @Bean
    public CustomerRepository customerRepository() {
        // return new PostgreSQLCustomerRepository(buildJdbcUrl(), dbUsername, dbPassword);
        return new InMemoryCustomerRepository();
    }

    @Bean
    public BookRepository bookRepository() {
        // return new PostgreSQLBookRepository(buildJdbcUrl(), dbUsername, dbPassword);
        return new InMemoryBookRepository();
    }
}
