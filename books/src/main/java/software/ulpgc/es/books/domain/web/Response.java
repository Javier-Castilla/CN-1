package software.ulpgc.es.books.domain.web;

import java.util.Optional;

public interface Response {
    int status();
    boolean hasBody();
    Optional<Object> body();
}
