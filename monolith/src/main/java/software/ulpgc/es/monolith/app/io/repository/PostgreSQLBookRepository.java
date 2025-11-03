package software.ulpgc.es.monolith.app.io.repository;

import software.ulpgc.es.monolith.domain.model.Book;
import software.ulpgc.es.monolith.domain.io.repository.BookRepository;
import software.ulpgc.es.monolith.domain.model.ISBN;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostgreSQLBookRepository implements BookRepository {

    private static PostgreSQLBookRepository instance;
    private Connection connection;

    private final String url;
    private final String user;
    private final String password;

    private PostgreSQLBookRepository(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
        connectAndInitialize();
    }

    public static synchronized PostgreSQLBookRepository getInstance(String url, String user, String password) {
        if (instance == null) {
            instance = new PostgreSQLBookRepository(url, user, password);
        }
        return instance;
    }

    private void connectAndInitialize() {
        try {
            this.connection = DriverManager.getConnection(url, user, password);
            String sql = """
                CREATE TABLE IF NOT EXISTS monolith (
                    isbn VARCHAR(13) PRIMARY KEY,
                    title VARCHAR(255) NOT NULL,
                    author VARCHAR(255) NOT NULL,
                    publisher VARCHAR(255),
                    stock INT NOT NULL
                );
            """;

            try (Statement stmt = connection.createStatement()) {
                stmt.execute(sql);
                System.out.println("Table 'monolith' verified or created correctly.");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect or initialize database", e);
        }
    }

    private Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, user, password);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get database connection", e);
        }
        return connection;
    }

    @Override
    public List<Book> getAllBooks() {
        String sql = "SELECT * FROM monolith";
        List<Book> monolith = new ArrayList<>();

        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                monolith.add(new Book(
                        new ISBN(rs.getString("isbn")),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("publisher"),
                        rs.getInt("stock")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return monolith;
    }

    @Override
    public Book getBook(String isbn) {
        String sql = "SELECT * FROM monolith WHERE isbn = ?";

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, isbn);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Book(
                            new ISBN(rs.getString("isbn")),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getString("publisher"),
                            rs.getInt("stock")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean saveBook(Book book) {
        String sql = "INSERT INTO monolith (isbn, title, author, publisher, stock) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, book.isbn().getValue());
            pstmt.setString(2, book.title());
            pstmt.setString(3, book.author());
            pstmt.setString(4, book.publisher());
            pstmt.setInt(5, book.stock());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Book deleteBook(String isbn) {
        Book book = getBook(isbn);
        if (book == null) return null;

        String sql = "DELETE FROM monolith WHERE isbn = ?";

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, isbn);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return book;
    }

    @Override
    public Book updateBook(Book book) {
        String sql = "UPDATE monolith SET title = ?, author = ?, publisher = ?, stock = ? WHERE isbn = ?";

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, book.title());
            pstmt.setString(2, book.author());
            pstmt.setString(3, book.publisher());
            pstmt.setInt(4, book.stock());
            pstmt.setString(5, book.isbn().getValue());

            return pstmt.executeUpdate() > 0 ? book : null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
