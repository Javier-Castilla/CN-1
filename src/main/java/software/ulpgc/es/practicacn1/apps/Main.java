package software.ulpgc.es.practicacn1.apps;

import software.ulpgc.es.practicacn1.apps.repository.InMemoryBookRepository;
import software.ulpgc.es.practicacn1.domain.control.CommandFactory;
import software.ulpgc.es.practicacn1.domain.model.Book;
import software.ulpgc.es.practicacn1.domain.repository.BookRepository;

public class Main {
    public static void main(String[] args) {
        BookRepository repository = new InMemoryBookRepository();
        repository.saveBook(new Book("1234", "Pepe", "Juanito", "Manolo's SL"));
        System.out.println(repository.getBook("1234"));
    }

    public static CommandFactory getCommandFactory() {
        CommandFactory commandFactory = new CommandFactory();
        return commandFactory;
    }
}
