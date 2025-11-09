package software.ulpgc.es.orders.app.io.repository;

import software.ulpgc.es.orders.domain.model.ISBN;
import software.ulpgc.es.orders.domain.model.Order;
import software.ulpgc.es.orders.domain.model.OrderItem;
import software.ulpgc.es.orders.domain.io.repository.OrderRepository;
import software.ulpgc.es.orders.domain.io.repository.exceptions.InsufficientStockException;
import software.ulpgc.es.orders.domain.io.repository.exceptions.OrderNotFoundException;
import software.ulpgc.es.orders.domain.io.repository.exceptions.OrdersDatabaseException;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        Connection conn = DriverManager.getConnection(url, user, password);
        conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        return conn;
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

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(sqlOrders);
            stmt.execute(sqlItems);
        } catch (SQLException e) {
            throw new OrdersDatabaseException("Error initializing orders tables", e);
        }
    }

    private List<OrderItem> getOrderItems(int orderId, Connection conn) throws SQLException {
        String sqlItems = "SELECT book_isbn, quantity FROM order_items WHERE order_id = ?";
        List<OrderItem> items = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(sqlItems)) {
            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String isbnStr = rs.getString("book_isbn");
                    int quantity = rs.getInt("quantity");
                    items.add(new OrderItem(new ISBN(isbnStr), quantity));
                }
            }
        }
        return items;
    }

    private void reduceStock(Connection conn, OrderItem item) throws SQLException {
        String sqlCheckStock = "SELECT stock FROM books WHERE isbn = ? FOR UPDATE";
        String sqlUpdateStock = "UPDATE books SET stock = stock - ? WHERE isbn = ?";

        try (PreparedStatement checkStmt = conn.prepareStatement(sqlCheckStock)) {
            checkStmt.setString(1, item.isbn().getValue());
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (!rs.next()) {
                    throw new OrdersDatabaseException("Book not found: " + item.isbn().getValue(), null);
                }
                int stock = rs.getInt("stock");
                if (stock < item.quantity()) {
                    throw new InsufficientStockException(item.isbn().getValue(), item.quantity(), stock);
                }
            }
        }

        try (PreparedStatement updateStmt = conn.prepareStatement(sqlUpdateStock)) {
            updateStmt.setInt(1, item.quantity());
            updateStmt.setString(2, item.isbn().getValue());
            updateStmt.executeUpdate();
        }
    }

    private void increaseStock(Connection conn, OrderItem item) throws SQLException {
        String sql = "UPDATE books SET stock = stock + ? WHERE isbn = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, item.quantity());
            pstmt.setString(2, item.isbn().getValue());
            pstmt.executeUpdate();
        }
    }

    @Override
    public Order getOrder(int id) {
        String sql = "SELECT id AS order_id, customer_id, order_date FROM orders WHERE id = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    throw new OrderNotFoundException(id);
                }
                int customerId = rs.getInt("customer_id");
                Timestamp ts = rs.getTimestamp("order_date");
                LocalDateTime date = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
                List<OrderItem> items = getOrderItems(id, conn);
                return new Order(id, customerId, date, items);
            }
        } catch (SQLException e) {
            throw new OrdersDatabaseException("Error fetching order with id " + id, e);
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
            throw new OrdersDatabaseException("Error retrieving all orders", e);
        }
        return orders;
    }

    @Override
    public List<Order> getOrdersByCustomer(int customerId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT id FROM orders WHERE customer_id = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(getOrder(rs.getInt("id")));
                }
            }
        } catch (SQLException e) {
            throw new OrdersDatabaseException("Error retrieving orders for customer " + customerId, e);
        }
        return orders;
    }

    @Override
    public Order saveOrder(Order order) {
        String sqlInsertOrder = "INSERT INTO orders (customer_id, order_date) VALUES (?, ?) RETURNING id";
        String sqlInsertItem = "INSERT INTO order_items (order_id, book_isbn, quantity) VALUES (?, ?, ?)";

        Connection conn = null;
        try {
            conn = connect();
            conn.setAutoCommit(false);

            for (OrderItem item : order.items()) {
                reduceStock(conn, item);
            }

            int orderId;
            try (PreparedStatement pstmt = conn.prepareStatement(sqlInsertOrder)) {
                pstmt.setInt(1, order.customerId());
                pstmt.setTimestamp(2, Timestamp.valueOf(order.date()));
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) orderId = rs.getInt("id");
                    else throw new OrdersDatabaseException("Failed to insert order, no ID returned", null);
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
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    throw new OrdersDatabaseException("Error rolling back transaction", rollbackEx);
                }
            }
            throw new OrdersDatabaseException("Error saving order", e);
        } finally {
            closeConnection(conn);
        }
    }

    @Override
    public Order updateOrder(Order order) {
        String sqlUpdateOrder = "UPDATE orders SET customer_id = ?, order_date = ? WHERE id = ?";
        String sqlDeleteItems = "DELETE FROM order_items WHERE order_id = ?";
        String sqlInsertItem = "INSERT INTO order_items (order_id, book_isbn, quantity) VALUES (?, ?, ?)";

        Connection conn = null;
        try {
            conn = connect();
            conn.setAutoCommit(false);

            List<OrderItem> oldItems = getOrderItems(order.id(), conn);

            try (PreparedStatement pstmt = conn.prepareStatement(sqlUpdateOrder)) {
                pstmt.setInt(1, order.customerId());
                pstmt.setTimestamp(2, Timestamp.valueOf(order.date()));
                pstmt.setInt(3, order.id());
                int affectedRows = pstmt.executeUpdate();

                if (affectedRows == 0) {
                    throw new OrderNotFoundException(order.id());
                }
            }

            Map<String, Integer> oldMap = oldItems.stream()
                    .collect(Collectors.toMap(i -> i.isbn().getValue(), OrderItem::quantity));

            for (OrderItem newItem : order.items()) {
                String isbn = newItem.isbn().getValue();
                int oldQty = oldMap.getOrDefault(isbn, 0);
                int diff = newItem.quantity() - oldQty;

                if (diff > 0) {
                    reduceStock(conn, new OrderItem(newItem.isbn(), diff));
                } else if (diff < 0) {
                    increaseStock(conn, new OrderItem(newItem.isbn(), -diff));
                }
                oldMap.remove(isbn);
            }

            for (Map.Entry<String, Integer> removed : oldMap.entrySet()) {
                increaseStock(conn, new OrderItem(new ISBN(removed.getKey()), removed.getValue()));
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
            conn.setAutoCommit(true);

            return order;

        } catch (SQLException | OrderNotFoundException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    throw new OrdersDatabaseException("Error rolling back transaction during update", rollbackEx);
                }
            }
            if (e instanceof OrderNotFoundException) {
                throw (OrderNotFoundException) e;
            }
            throw new OrdersDatabaseException("Error updating order with id " + order.id(), e);
        } finally {
            closeConnection(conn);
        }
    }

    @Override
    public Order cancelOrder(int id) {
        Order order = getOrder(id);
        String sqlDeleteItems = "DELETE FROM order_items WHERE order_id = ?";
        String sqlDeleteOrder = "DELETE FROM orders WHERE id = ?";
        String sqlUpdateStock = "UPDATE books SET stock = stock + ? WHERE isbn = ?";

        Connection conn = null;
        try {
            conn = connect();
            conn.setAutoCommit(false);

            if (order.items() != null) {
                try (PreparedStatement updateStockStmt = conn.prepareStatement(sqlUpdateStock)) {
                    for (OrderItem item : order.items()) {
                        updateStockStmt.setInt(1, item.quantity());
                        updateStockStmt.setString(2, item.isbn().getValue());
                        updateStockStmt.addBatch();
                    }
                    updateStockStmt.executeBatch();
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sqlDeleteItems)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sqlDeleteOrder)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
            }

            conn.commit();
            conn.setAutoCommit(true);
            return order;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    throw new OrdersDatabaseException("Error rolling back transaction during cancel", rollbackEx);
                }
            }
            throw new OrdersDatabaseException("Error canceling order with id " + id, e);
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
