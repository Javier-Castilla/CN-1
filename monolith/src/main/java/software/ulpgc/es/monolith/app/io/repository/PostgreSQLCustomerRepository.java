package software.ulpgc.es.monolith.app.io.repository;

import software.ulpgc.es.monolith.domain.io.repository.CustomerRepository;
import software.ulpgc.es.monolith.domain.model.Customer;
import software.ulpgc.es.monolith.domain.io.repository.exceptions.customers.CustomerNotFoundException;
import software.ulpgc.es.monolith.domain.io.repository.exceptions.customers.DuplicateCustomerException;
import software.ulpgc.es.monolith.domain.io.repository.exceptions.customers.CustomersDatabaseException;

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
        Connection conn = DriverManager.getConnection(url, user, password);
        conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        return conn;
    }

    private void initialize() {
        String sql = """
            CREATE TABLE IF NOT EXISTS customers (
                id SERIAL PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                email VARCHAR(100) UNIQUE NOT NULL
            );
        """;

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new CustomersDatabaseException("Error initializing Customer repository", e);
        }
    }

    @Override
    public Customer getCustomer(int id) {
        String sql = "SELECT id, name, email FROM customers WHERE id = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Customer(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("email")
                    );
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
        String sql = "SELECT id, name, email FROM customers";
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
            return customers;
        } catch (SQLException e) {
            throw new CustomersDatabaseException("Error retrieving all customers", e);
        }
    }

    @Override
    public Customer saveCustomer(Customer customer) {
        String sql = "INSERT INTO customers (name, email) VALUES (?, ?) RETURNING id";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, customer.name());
            pstmt.setString(2, customer.email());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Customer(
                            rs.getInt("id"),
                            customer.name(),
                            customer.email()
                    );
                } else {
                    throw new CustomersDatabaseException("Failed to insert customer, no ID returned", null);
                }
            }
        } catch (SQLException e) {
            if (e.getSQLState() != null && e.getSQLState().startsWith("23")) {
                throw new DuplicateCustomerException(customer.email());
            }
            throw new CustomersDatabaseException("Error saving customer", e);
        }
    }

    @Override
    public Customer updateCustomer(Customer customer) {
        String sql = "UPDATE customers SET name = ?, email = ? WHERE id = ?";

        Connection conn = null;
        try {
            conn = connect();
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, customer.name());
                pstmt.setString(2, customer.email());
                pstmt.setInt(3, customer.id());
                int rows = pstmt.executeUpdate();

                if (rows == 0) {
                    throw new CustomerNotFoundException(customer.id());
                }
            }

            conn.commit();
            conn.setAutoCommit(true);

            return customer;

        } catch (SQLException | CustomerNotFoundException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    throw new CustomersDatabaseException("Error rolling back transaction", rollbackEx);
                }
            }
            if (e instanceof CustomerNotFoundException) {
                throw (CustomerNotFoundException) e;
            }
            if (e instanceof SQLException && ((SQLException) e).getSQLState() != null
                    && ((SQLException) e).getSQLState().startsWith("23")) {
                throw new DuplicateCustomerException(customer.email());
            }
            throw new CustomersDatabaseException("Error updating customer", e);
        } finally {
            closeConnection(conn);
        }
    }

    @Override
    public Customer deleteCustomer(int id) {
        Customer customer = getCustomer(id);
        String checkOrdersSql = "SELECT COUNT(*) FROM orders WHERE customer_id = ?";
        String deleteSql = "DELETE FROM customers WHERE id = ?";

        Connection conn = null;
        try {
            conn = connect();
            conn.setAutoCommit(false);

            try (PreparedStatement checkStmt = conn.prepareStatement(checkOrdersSql)) {
                checkStmt.setInt(1, id);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        throw new CustomersDatabaseException(
                                "Cannot delete customer with ID " + id + " because they have existing orders.",
                                null
                        );
                    }
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
            }

            conn.commit();
            conn.setAutoCommit(true);

            return customer;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    throw new CustomersDatabaseException("Error rolling back transaction", rollbackEx);
                }
            }
            throw new CustomersDatabaseException("Error deleting customer with ID " + id, e);
        } finally {
            closeConnection(conn);
        }
    }

    private void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.getAutoCommit()) {
                    conn.setAutoCommit(true);
                }
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}
