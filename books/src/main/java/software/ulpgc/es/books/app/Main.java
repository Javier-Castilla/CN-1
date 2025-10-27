package software.ulpgc.es.books.app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import software.ulpgc.es.books.app.repository.PostgreSQLBookRepository;
import software.ulpgc.es.books.domain.control.CommandFactory;
import software.ulpgc.es.books.domain.repository.BookRepository;

@SpringBootApplication(scanBasePackages = "software.ulpgc.es.books")
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
        SpringApplication.run(software.ulpgc.es.books.app.Main.class, args);
    }

    private String buildJdbcUrl() {
        return String.format("jdbc:%s://%s:%s/%s", dbType, dbHost, dbPort, dbName);
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
