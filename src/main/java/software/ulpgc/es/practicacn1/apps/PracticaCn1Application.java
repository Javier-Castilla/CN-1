package software.ulpgc.es.practicacn1.apps;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import software.ulpgc.es.practicacn1.domain.control.CommandFactory;

@SpringBootApplication
public class PracticaCn1Application {

    public static void main(String[] args) {
        SpringApplication.run(PracticaCn1Application.class, args);
    }

    @Bean
    public CommandFactory commandFactory() {
        CommandFactory commandFactory = new CommandFactory();
        return commandFactory;
    }
}
