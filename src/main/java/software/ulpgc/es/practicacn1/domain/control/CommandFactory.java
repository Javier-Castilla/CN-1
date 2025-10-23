package software.ulpgc.es.practicacn1.architecture.control;

import software.ulpgc.es.practicacn1.architecture.Request;
import software.ulpgc.es.practicacn1.architecture.Response;

import java.util.HashMap;
import java.util.Map;

public class CommandFactory {
    private final Map<String, Builder> builders;

    public CommandFactory() {
        this.builders = new HashMap<>();
    }

    public Selector with(Request request, Response response) {
        return name -> builders.get(name).build(request, response);
    }

    public CommandFactory register(String name, Builder builder) {
        this.builders.put(name, builder);
        return this;
    }

    public interface Builder {
        Command build(Request request, Response response);
    }

    public interface Selector {
        Command build(String name);
    }
}
