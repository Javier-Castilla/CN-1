package software.ulpgc.es.books.app.io.repository;

import software.ulpgc.es.books.domain.model.Book;
import software.ulpgc.es.books.domain.io.repository.BookRepository;
import software.ulpgc.es.books.domain.io.repository.exceptions.*;
import software.ulpgc.es.books.domain.model.ISBN;

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
                CREATE TABLE IF NOT EXISTS books (
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
            throw new BooksDatabaseException("Failed to connect or initialize database", e);
        }
    }

    private Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, user, password);
            }
        } catch (SQLException e) {
            throw new BooksDatabaseException("Failed to get database connection", e);
        }
        return connection;
    }

    @Override
    public List<Book> getAllBooks() {
        String sql = "SELECT * FROM books";
        List<Book> books = new ArrayList<>();

        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                System.out.println(rs.getString("title"));
                books.add(new Book(
                        new ISBN(rs.getString("isbn")),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("publisher"),
                        rs.getInt("stock")
                ));
            }

        } catch (Exception e) {
            throw new BooksDatabaseException("Error fetching all books", e);
        }

        return books;
    }

    @Override
    public Book getBook(String isbn) {
        String sql = "SELECT * FROM books WHERE isbn = ?";

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
                } else {
                    throw new BookNotFoundException("Book with ISBN " + isbn + " not found");
                }
            }
        } catch (SQLException e) {
            throw new BooksDatabaseException("Error fetching book with ISBN " + isbn, e);
        }
    }

    @Override
    public boolean saveBook(Book book) {
        String sql = "INSERT INTO books (isbn, title, author, publisher, stock) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, book.isbn().getValue());
            pstmt.setString(2, book.title());
            pstmt.setString(3, book.author());
            pstmt.setString(4, book.publisher());
            pstmt.setInt(5, book.stock());

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) { // PostgreSQL code for unique_violation
                throw new DuplicateBookException("Book with ISBN " + book.isbn().getValue() + " already exists");
            }
            throw new BooksDatabaseException("Error saving book " + book.title(), e);
        }
    }

    @Override
    public Book deleteBook(String isbn) {
        Book book = getBook(isbn); // throws if not found

        String sql = "DELETE FROM books WHERE isbn = ?";

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, isbn);
            pstmt.executeUpdate();
            return book;
        } catch (SQLException e) {
            throw new BooksDatabaseException("Error deleting book with ISBN " + isbn, e);
        }
    }

    @Override
    public Book updateBook(Book book) {
        String sql = "UPDATE books SET title = ?, author = ?, publisher = ?, stock = ? WHERE isbn = ?";

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, book.title());
            pstmt.setString(2, book.author());
            pstmt.setString(3, book.publisher());
            pstmt.setInt(4, book.stock());
            pstmt.setString(5, book.isbn().getValue());

            int affected = pstmt.executeUpdate();
            if (affected == 0) {
                throw new BookNotFoundException("Book with ISBN " + book.isbn().getValue() + " not found for update");
            }

            return book;
        } catch (SQLException e) {
            throw new BooksDatabaseException("Error updating book with ISBN " + book.isbn().getValue(), e);
        }
    }
}
