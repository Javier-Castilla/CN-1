package software.ulpgc.es.orders.app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import software.ulpgc.es.orders.app.io.repository.PostgreSQLOrderRepository;
import software.ulpgc.es.orders.domain.repository.OrderRepository;

@SpringBootApplication(scanBasePackages = "software.ulpgc.es.orders")
@Profile("orders")
public class Main {
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
        SpringApplication.run(software.ulpgc.es.orders.app.Main.class, args);
    }

    private String buildJdbcUrl() {
        return String.format("jdbc:%s://%s:%s/%s", dbType, dbHost, dbPort, dbName);
    }

    @Bean
    public OrderRepository orderRepository() {
        return new PostgreSQLOrderRepository(buildJdbcUrl(), dbUsername, dbPassword);
    }
}
