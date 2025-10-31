package software.ulpgc.es.customers.app.io.repository;

import software.ulpgc.es.customers.domain.model.Customer;
import software.ulpgc.es.customers.domain.io.repository.CustomerRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostgreSQLCustomerRepository implements CustomerRepository {

    private final String url;
    private final String user;
    private final String password;

    public PostgreSQLCustomerRepository(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
        initialize();
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public void initialize() {
        String sql = """
            CREATE TABLE IF NOT EXISTS customers (
                id SERIAL PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                email VARCHAR(100) UNIQUE NOT NULL
            );
        """;

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Table 'customers' verified or created correctly.");
        } catch (SQLException e) {
            System.err.println("Error while initializing table 'customers': " + e.getMessage());
        }
    }

    @Override
    public Customer getCustomer(int id) {
        String sql = "SELECT * FROM customers WHERE id = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Customer(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public List<Customer> getAllCustomers() {
        String sql = "SELECT * FROM customers";
        List<Customer> customers = new ArrayList<>();

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                customers.add(new Customer(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return customers;
    }

    @Override
    public Customer saveCustomer(Customer customer) {
        String sql = "INSERT INTO customers (name, email) VALUES (?, ?) RETURNING id";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, customer.name());
            pstmt.setString(2, customer.email());

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Customer(
                        rs.getInt("id"),
                        customer.name(),
                        customer.email()
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Customer updateCustomer(Customer customer) {
        String sql = "UPDATE customers SET name = ?, email = ? WHERE id = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, customer.name());
            pstmt.setString(2, customer.email());
            pstmt.setInt(3, customer.id());

            int rows = pstmt.executeUpdate();
            return rows > 0 ? customer : null;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Customer deleteCustomer(int id) {
        Customer customer = getCustomer(id);
        if (customer == null) return null;

        String sql = "DELETE FROM customers WHERE id = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return customer;
    }
}
