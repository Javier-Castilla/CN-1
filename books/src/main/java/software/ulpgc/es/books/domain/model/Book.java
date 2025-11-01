package software.ulpgc.es.books.domain.model;

public record Book(ISBN isbn, String title, String author, String publisher, int stock) {
}
