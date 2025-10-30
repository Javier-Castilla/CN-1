package software.ulpgc.es.monolith.app.repository;

import software.ulpgc.es.monolith.domain.model.ISBN;
import software.ulpgc.es.monolith.domain.model.Order;
import software.ulpgc.es.monolith.domain.model.OrderItem;
import software.ulpgc.es.monolith.domain.io.repository.OrderRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PostgreSQLOrderRepository implements OrderRepository {
    private final String url;
    private final String user;
    private final String password;

    public PostgreSQLOrderRepository(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
        initialize();
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public void initialize() {
        String sqlOrders = """
            CREATE TABLE IF NOT EXISTS orders (
                id SERIAL PRIMARY KEY,
                customer_id INT NOT NULL REFERENCES customers(id),
                order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        """;

        String sqlItems = """
            CREATE TABLE IF NOT EXISTS order_items (
                order_id INT NOT NULL REFERENCES orders(id),
                book_isbn VARCHAR(20) NOT NULL REFERENCES books(isbn),
                quantity INT NOT NULL,
                PRIMARY KEY (order_id, book_isbn)
            );
        """;

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sqlOrders);
            stmt.execute(sqlItems);
            System.out.println("Tables 'orders' and 'order_items' verified or created correctly.");
        } catch (SQLException e) {
            System.err.println("Error initializing tables: " + e.getMessage());
        }
    }

    private List<OrderItem> getOrderItems(int orderId, Connection conn) throws SQLException {
        String sqlItems = "SELECT book_isbn, quantity FROM order_items WHERE order_id = ?";
        List<OrderItem> items = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(sqlItems)) {
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String isbnStr = rs.getString("book_isbn");
                int quantity = rs.getInt("quantity");
                items.add(new OrderItem(new ISBN(isbnStr), quantity));
            }
        }

        return items;
    }

    @Override
    public Order getOrder(int id) {
        String sql = """
            SELECT id AS order_id, customer_id, order_date
            FROM orders
            WHERE id = ?
        """;

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) return null;

            int customerId = rs.getInt("customer_id");
            Timestamp ts = rs.getTimestamp("order_date");
            LocalDateTime date = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();

            List<OrderItem> items = getOrderItems(id, conn);

            return new Order(id, customerId, date, items);

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching order with id " + id, e);
        }
    }

    @Override
    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT id FROM orders";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                orders.add(getOrder(rs.getInt("id")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return orders;
    }

    @Override
    public List<Order> getOrdersByCustomer(int customerId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT id FROM orders WHERE customer_id = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                orders.add(getOrder(rs.getInt("id")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return orders;
    }

    @Override
    public Order saveOrder(Order order) {
        String sqlInsertOrder = "INSERT INTO orders (customer_id, order_date) VALUES (?, ?) RETURNING id";
        String sqlInsertItem = "INSERT INTO order_items (order_id, book_isbn, quantity) VALUES (?, ?, ?)";

        try (Connection conn = connect()) {
            conn.setAutoCommit(false);

            int orderId;
            try (PreparedStatement pstmt = conn.prepareStatement(sqlInsertOrder)) {
                pstmt.setInt(1, order.customerId());
                pstmt.setTimestamp(2, Timestamp.valueOf(order.date()));
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) orderId = rs.getInt("id");
                else {
                    conn.rollback();
                    return null;
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sqlInsertItem)) {
                for (OrderItem item : order.items()) {
                    pstmt.setInt(1, orderId);
                    pstmt.setString(2, item.isbn().getValue());
                    pstmt.setInt(3, item.quantity());
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }

            conn.commit();
            return new Order(orderId, order.customerId(), order.date(), order.items());

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Order updateOrder(Order order) {
        String sqlUpdateOrder = "UPDATE orders SET customer_id = ?, order_date = ? WHERE id = ?";
        String sqlDeleteItems = "DELETE FROM order_items WHERE order_id = ?";
        String sqlInsertItem = "INSERT INTO order_items (order_id, book_isbn, quantity) VALUES (?, ?, ?)";

        try (Connection conn = connect()) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(sqlUpdateOrder)) {
                pstmt.setInt(1, order.customerId());
                pstmt.setTimestamp(2, Timestamp.valueOf(order.date()));
                pstmt.setInt(3, order.id());
                int affected = pstmt.executeUpdate();
                if (affected == 0) {
                    conn.rollback();
                    return null;
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sqlDeleteItems)) {
                pstmt.setInt(1, order.id());
                pstmt.executeUpdate();
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sqlInsertItem)) {
                for (OrderItem item : order.items()) {
                    pstmt.setInt(1, order.id());
                    pstmt.setString(2, item.isbn().getValue());
                    pstmt.setInt(3, item.quantity());
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }

            conn.commit();
            return order;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Order cancelOrder(int id) {
        Order order = getOrder(id);
        if (order == null) return null;

        String sqlDeleteItems = "DELETE FROM order_items WHERE order_id = ?";
        String sqlDeleteOrder = "DELETE FROM orders WHERE id = ?";

        try (Connection conn = connect()) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(sqlDeleteItems)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sqlDeleteOrder)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
            }

            conn.commit();
            return order;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
