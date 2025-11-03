package software.ulpgc.es.monolith.app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import software.ulpgc.es.monolith.app.io.repository.PostgreSQLBookRepository;
import software.ulpgc.es.monolith.app.io.repository.PostgreSQLCustomerRepository;
import software.ulpgc.es.monolith.app.io.repository.PostgreSQLOrderRepository;
import software.ulpgc.es.monolith.domain.control.CommandFactory;
import software.ulpgc.es.monolith.domain.control.books.*;
import software.ulpgc.es.monolith.domain.control.customers.*;
import software.ulpgc.es.monolith.domain.control.orders.*;
import software.ulpgc.es.monolith.domain.io.repository.BookRepository;
import software.ulpgc.es.monolith.domain.io.repository.CustomerRepository;
import software.ulpgc.es.monolith.domain.io.repository.OrderRepository;

@SpringBootApplication(scanBasePackages = "software.ulpgc.es.monolith")
@Profile("monolith")
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
        SpringApplication.run(Main.class, args);
    }

    private String buildJdbcUrl() {
        return String.format("jdbc:%s://%s:%s/%s", dbType, dbHost, dbPort, dbName);
    }

    @Bean
    public OrderRepository orderRepository() {
        return new PostgreSQLOrderRepository(buildJdbcUrl(), dbUsername, dbPassword);
        //return new InMemoryOrderRepository();
    }

    @Bean
    public CustomerRepository customerRepository() {
        return new PostgreSQLCustomerRepository(buildJdbcUrl(), dbUsername, dbPassword);
        //return new InMemoryCustomerRepository();
    }

    @Bean
    public BookRepository bookRepository() {
        return new PostgreSQLBookRepository(buildJdbcUrl(), dbUsername, dbPassword);
        // return new InMemoryBookRepository();
    }

    @Bean
    public CommandFactory commandFactory(BookRepository bookRepository, CustomerRepository customerRepository, OrderRepository orderRepository) {
        CommandFactory factory = new CommandFactory();
        factory.register("createBook",
                (CreateBookCommand.Input input, CreateBookCommand.Output output) -> new CreateBookCommand(input, output, bookRepository))
                .register("getBook",
                        (GetBookCommand.Input input, GetBookCommand.Output output) -> new GetBookCommand(input, output, bookRepository))
                .register("getBooks",
                        (NoInput input, GetAllBooksCommand.Output output) -> new GetAllBooksCommand(output, bookRepository))
                .register("updateBook",
                        (UpdateBookCommand.Input input, UpdateBookCommand.Output output) -> new UpdateBookCommand(input, output, bookRepository))
                .register("deleteBook",
                        (DeleteBookCommand.Input input, DeleteBookCommand.Output output) -> new DeleteBookCommand(input, output, bookRepository))
                .register("createCustomer",
                        (CreateCustomerCommand.Input input, CreateCustomerCommand.Output output) -> new CreateCustomerCommand(input, output, customerRepository))
                .register("getCustomer",
                        (GetCustomerCommand.Input input, GetCustomerCommand.Output output) -> new GetCustomerCommand(input, output, customerRepository))
                .register("getCustomers",
                        (NoInput input, GetAllCustomersCommand.Output output) -> new GetAllCustomersCommand(output, customerRepository))
                .register("updateCustomer",
                        (UpdateCustomerCommand.Input input, UpdateCustomerCommand.Output output) -> new UpdateCustomerCommand(input, output, customerRepository))
                .register("deleteCustomer",
                        (DeleteCustomerCommand.Input input, DeleteCustomerCommand.Output output) -> new DeleteCustomerCommand(input, output, customerRepository))
                .register("createOrder",
                        (CreateOrderCommand.Input input, CreateOrderCommand.Output output) -> new CreateOrderCommand(input, output, orderRepository))
                .register("getCustomerOrder",
                        (GetCustomerOrderCommand.Input input, GetCustomerOrderCommand.Output output) -> new GetCustomerOrderCommand(input, output, orderRepository))
                .register("getCustomersOrders",
                        (GetCustomerOrdersCommand.Input input, GetCustomerOrdersCommand.Output output) -> new GetCustomerOrdersCommand(input, output, orderRepository))
                .register("getOrders",
                        (NoInput input, GetAllOrdersCommand.Output output) -> new GetAllOrdersCommand(output, orderRepository))
                .register("updateOrder",
                        (UpdateOrderCommand.Input input, UpdateOrderCommand.Output output) -> new UpdateOrderCommand(input, output, orderRepository))
                .register("deleteOrder",
                        (DeleteOrderCommand.Input input, DeleteOrderCommand.Output output) -> new DeleteOrderCommand(input, output, orderRepository));
        return factory;
    }

    public static final class NoInput {
        public static final NoInput INSTANCE = new NoInput();
        private NoInput() {}
    }
}
