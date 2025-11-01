package software.ulpgc.es.monolith.app.repository;

import software.ulpgc.es.monolith.domain.model.Book;
import software.ulpgc.es.monolith.domain.io.repository.BookRepository;
import software.ulpgc.es.monolith.domain.model.ISBN;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostgreSQLBookRepository implements BookRepository {

    private final String url;
    private final String user;
    private final String password;

    public PostgreSQLBookRepository(String url, String user, String password) {
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
            CREATE TABLE IF NOT EXISTS books (
                isbn VARCHAR(13) PRIMARY KEY,
                title VARCHAR(255) NOT NULL,
                author VARCHAR(255) NOT NULL,
                publisher VARCHAR(255),
                stock INT NOT NULL
            );
        """;

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);
            System.out.println("Table 'books' verified or created correctly.");

        } catch (SQLException e) {
            System.err.println("Error while initializing table 'books': " + e.getMessage());
        }
    }

    @Override
    public List<Book> getAllBooks() {
        String sql = "SELECT * FROM books";
        List<Book> books = new ArrayList<>();

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                books.add(new Book(
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

        return books;
    }

    @Override
    public Book getBook(String isbn) {
        String sql = "SELECT * FROM books WHERE isbn = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, isbn);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Book(
                        new ISBN(rs.getString("isbn")),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("publisher"),
                        rs.getInt("stock")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean saveBook(Book book) {
        String sql = "INSERT INTO books (isbn, title, author, publisher, stock) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, book.isbn().getValue());
            pstmt.setString(2, book.title());
            pstmt.setString(3, book.author());
            pstmt.setString(4, book.publisher());
            pstmt.setInt(5, book.stock());

            int rows = pstmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Book deleteBook(String isbn) {
        Book book = getBook(isbn);
        if (book == null) return null;

        String sql = "DELETE FROM books WHERE isbn = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, isbn);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return book;
    }

    @Override
    public Book updateBook(Book book) {
        String sql = "UPDATE books SET title = ?, author = ?, publisher = ?, stock = ? WHERE isbn = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, book.title());
            pstmt.setString(2, book.author());
            pstmt.setString(3, book.publisher());
            pstmt.setInt(4, book.stock());
            pstmt.setString(5, book.isbn().getValue());

            int rows = pstmt.executeUpdate();
            return rows > 0 ? book : null;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}