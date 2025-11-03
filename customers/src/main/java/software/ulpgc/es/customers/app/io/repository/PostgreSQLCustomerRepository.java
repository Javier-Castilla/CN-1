package software.ulpgc.es.customers.app.io.repository;

import software.ulpgc.es.customers.domain.model.Customer;
import software.ulpgc.es.customers.domain.io.repository.CustomerRepository;
import software.ulpgc.es.customers.domain.io.repository.exceptions.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostgreSQLCustomerRepository implements CustomerRepository {

    private static PostgreSQLCustomerRepository instance;

    private Connection connection;
    private final String url;
    private final String user;
    private final String password;

    private PostgreSQLCustomerRepository(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
        initialize();
    }

    public static synchronized PostgreSQLCustomerRepository getInstance(String url, String user, String password) {
        if (instance == null) {
            instance = new PostgreSQLCustomerRepository(url, user, password);
        }
        return instance;
    }

    private void initialize() {
        try {
            connection = DriverManager.getConnection(url, user, password);
            String sql = """
                CREATE TABLE IF NOT EXISTS customers (
                    id SERIAL PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    email VARCHAR(100) UNIQUE NOT NULL
                );
            """;
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(sql);
                System.out.println("Table 'customers' verified or created correctly.");
            }
        } catch (SQLException e) {
            throw new CustomersDatabaseException("Error initializing Customer repository", e);
        }
    }

    private Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, user, password);
            }
            return connection;
        } catch (SQLException e) {
            throw new CustomersDatabaseException("Failed to get database connection", e);
        }
    }

    @Override
    public Customer getCustomer(int id) {
        String sql = "SELECT * FROM customers WHERE id = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Customer(rs.getInt("id"), rs.getString("name"), rs.getString("email"));
                } else {
                    throw new CustomerNotFoundException(id);
                }
            }
        } catch (SQLException e) {
            throw new CustomersDatabaseException("Error retrieving customer with ID " + id, e);
        }
    }

    @Override
    public List<Customer> getAllCustomers() {
        String sql = "SELECT * FROM customers";
        List<Customer> customers = new ArrayList<>();
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                customers.add(new Customer(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email")
                ));
            }
            return customers;
        } catch (SQLException e) {
            throw new CustomersDatabaseException("Error retrieving all customers", e);
        }
    }

    @Override
    public Customer saveCustomer(Customer customer) {
        String sql = "INSERT INTO customers (name, email) VALUES (?, ?) RETURNING id";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, customer.name());
            pstmt.setString(2, customer.email());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Customer(rs.getInt("id"), customer.name(), customer.email());
                } else {
                    throw new CustomersDatabaseException("Failed to insert customer, no ID returned", null);
                }
            }
        } catch (SQLException e) {
            // Código SQLState que empieza por 23 indica violación de restricción (unique, foreign key, etc.)
            if (e.getSQLState() != null && e.getSQLState().startsWith("23")) {
                throw new DuplicateCustomerException(customer.email());
            }
            throw new CustomersDatabaseException("Error saving customer", e);
        }
    }

    @Override
    public Customer updateCustomer(Customer customer) {
        String sql = "UPDATE customers SET name = ?, email = ? WHERE id = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, customer.name());
            pstmt.setString(2, customer.email());
            pstmt.setInt(3, customer.id());
            int rows = pstmt.executeUpdate();

            if (rows == 0) {
                throw new CustomerNotFoundException(customer.id());
            }
            return customer;
        } catch (SQLException e) {
            if (e.getSQLState() != null && e.getSQLState().startsWith("23")) {
                throw new DuplicateCustomerException(customer.email());
            }
            throw new CustomersDatabaseException("Error updating customer", e);
        }
    }

    @Override
    public Customer deleteCustomer(int id) {
        Customer customer = getCustomer(id); // Lanzará OrderNotFoundException si no existe
        String sql = "DELETE FROM customers WHERE id = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            return customer;
        } catch (SQLException e) {
            throw new CustomersDatabaseException("Error deleting customer with ID " + id, e);
        }
    }
}
