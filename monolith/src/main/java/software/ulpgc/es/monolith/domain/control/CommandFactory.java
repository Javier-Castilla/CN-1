package software.ulpgc.es.monolith.domain.control;

import java.util.HashMap;
import java.util.Map;

public class CommandFactory {

    private final Map<String, Builder<?, ?, ?>> builders = new HashMap<>();

    public <I, O> Selector<I, O> with(I input, O output) {
        return name -> {
            Builder<I, O, ? extends Command> builder =
                    (Builder<I, O, ? extends Command>) builders.get(name);
            if (builder == null) {
                throw new IllegalArgumentException("No builder registered for: " + name);
            }
            return builder.build(input, output);
        };
    }

    public interface Builder<I, O, C extends Command> {
        C build(I input, O output);
    }

    public interface Selector<I, O> {
        Command build(String name);
    }

    public <I, O, C extends Command> CommandFactory register(String name, Builder<I, O, C> builder) {
        builders.put(name, builder);
        return this;
    }
}
