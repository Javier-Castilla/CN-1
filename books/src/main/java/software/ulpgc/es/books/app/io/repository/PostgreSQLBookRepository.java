package software.ulpgc.es.books.app.io.repository;

import software.ulpgc.es.books.domain.io.repository.exceptions.BookNotFoundException;
import software.ulpgc.es.books.domain.io.repository.exceptions.BooksDatabaseException;
import software.ulpgc.es.books.domain.io.repository.exceptions.DuplicateBookException;
import software.ulpgc.es.books.domain.model.Book;
import software.ulpgc.es.books.domain.io.repository.BookRepository;
import software.ulpgc.es.books.domain.model.ISBN;

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
        Connection conn = DriverManager.getConnection(url, user, password);
        conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        return conn;
    }

    private void initialize() {
        String sql = """
            CREATE TABLE IF NOT EXISTS books (
                isbn VARCHAR(13) PRIMARY KEY,
                title VARCHAR(255) NOT NULL,
                author VARCHAR(255) NOT NULL,
                publisher VARCHAR(255),
                stock INT NOT NULL
            );
        """;

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new BooksDatabaseException("Failed to connect or initialize database", e);
        }
    }

    @Override
    public List<Book> getAllBooks() {
        String sql = "SELECT isbn, title, author, publisher, stock FROM books";
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

        } catch (Exception e) {
            throw new BooksDatabaseException("Error fetching all books", e);
        }

        return books;
    }

    @Override
    public Book getBook(String isbn) {
        String sql = "SELECT isbn, title, author, publisher, stock FROM books WHERE isbn = ?";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
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

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, book.isbn().getValue());
            pstmt.setString(2, book.title());
            pstmt.setString(3, book.author());
            pstmt.setString(4, book.publisher());
            pstmt.setInt(5, book.stock());

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new DuplicateBookException("Book with ISBN " + book.isbn().getValue() + " already exists");
            }
            throw new BooksDatabaseException("Error saving book " + book.title(), e);
        }
    }

    @Override
    public Book updateBook(Book book) {
        String sql = "UPDATE books SET title = ?, author = ?, publisher = ?, stock = ? WHERE isbn = ?";

        Connection conn = null;
        try {
            conn = connect();
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, book.title());
                pstmt.setString(2, book.author());
                pstmt.setString(3, book.publisher());
                pstmt.setInt(4, book.stock());
                pstmt.setString(5, book.isbn().getValue());

                int affected = pstmt.executeUpdate();
                if (affected == 0) {
                    throw new BookNotFoundException("Book with ISBN " + book.isbn().getValue() + " not found for update");
                }
            }

            conn.commit();
            conn.setAutoCommit(true);

            return book;

        } catch (SQLException | BookNotFoundException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    throw new BooksDatabaseException("Error rolling back transaction", rollbackEx);
                }
            }
            if (e instanceof BookNotFoundException) {
                throw (BookNotFoundException) e;
            }
            throw new BooksDatabaseException("Error updating book with ISBN " + book.isbn().getValue(), e);
        } finally {
            closeConnection(conn);
        }
    }

    @Override
    public Book deleteBook(String isbn) {
        Book book = getBook(isbn);
        String checkSql = "SELECT COUNT(*) FROM order_items WHERE book_isbn = ?";
        String deleteSql = "DELETE FROM books WHERE isbn = ?";

        Connection conn = null;
        try {
            conn = connect();
            conn.setAutoCommit(false);

            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, isbn);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        throw new BooksDatabaseException(
                                "Cannot delete book with ISBN " + isbn + " because it is associated with existing orders.",
                                null
                        );
                    }
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
                pstmt.setString(1, isbn);
                pstmt.executeUpdate();
            }

            conn.commit();
            conn.setAutoCommit(true);

            return book;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    throw new BooksDatabaseException("Error rolling back transaction", rollbackEx);
                }
            }
            throw new BooksDatabaseException("Error deleting book with ISBN " + isbn, e);
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
