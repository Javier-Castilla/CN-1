package software.ulpgc.es.monolith.domain.model;

public record Book(ISBN isbn, String title, String author, String publisher, int stock) {
}
