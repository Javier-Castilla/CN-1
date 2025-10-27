-- =============================================
-- TABLAS
-- =============================================

-- Tabla Books
CREATE TABLE IF NOT EXISTS books (
                                     isbn VARCHAR(20) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    publisher VARCHAR(255)
    );

-- Tabla Customers
CREATE TABLE IF NOT EXISTS customers (
                                         id SERIAL PRIMARY KEY,
                                         name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL
    );

-- Tabla Orders
CREATE TABLE IF NOT EXISTS orders (
                                      id SERIAL PRIMARY KEY,
                                      customer_id INT REFERENCES customers(id),
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Tabla Order_Items
CREATE TABLE IF NOT EXISTS order_items (
                                           order_id INT REFERENCES orders(id),
    book_isbn VARCHAR(20) REFERENCES books(isbn),
    quantity INT NOT NULL,
    PRIMARY KEY (order_id, book_isbn)
    );

-- =============================================
-- DATOS DE PRUEBA
-- =============================================

-- Books
INSERT INTO books (isbn, title, author, publisher) VALUES
                                                       ('978-1234567890', 'Clean Code', 'Robert C. Martin', 'Prentice Hall'),
                                                       ('978-0984782864', 'Effective Java', 'Joshua Bloch', 'Addison-Wesley');

-- Customers
INSERT INTO customers (name, email) VALUES
                                        ('Javier', 'javier@example.com'),
                                        ('Ana', 'ana@example.com');

-- Orders
INSERT INTO orders (customer_id, order_date) VALUES
                                                 (1, '2025-10-25 20:00:00'),
                                                 (2, '2025-10-26 15:30:00');

-- Order Items
INSERT INTO order_items (order_id, book_isbn, quantity) VALUES
                                                            (1, '978-1234567890', 2),
                                                            (1, '978-0984782864', 1),
                                                            (2, '978-0984782864', 3);
