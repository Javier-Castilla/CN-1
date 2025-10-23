package software.ulpgc.es.practicacn1.domain.control;

import java.util.Optional;

public interface Response {
    int status();
    boolean hasBody();
    Optional<Object> body();
}
