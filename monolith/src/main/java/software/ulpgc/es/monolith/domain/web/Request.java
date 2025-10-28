package software.ulpgc.es.monolith.domain.web;

import java.util.Map;
import java.util.Optional;

public interface Request {
    boolean hasBody();
    boolean hasParams();
    Map<String, Object> getParams();
    Optional<Object> body();
}
