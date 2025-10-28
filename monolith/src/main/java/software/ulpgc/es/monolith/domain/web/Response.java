package software.ulpgc.es.monolith.domain.web;

import java.util.Optional;

public interface Response {
    int status();
    boolean hasBody();
    Optional<Object> body();
}
