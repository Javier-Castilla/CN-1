package software.ulpgc.es.monolith.app.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.ulpgc.es.monolith.app.Main;
import software.ulpgc.es.monolith.domain.control.CommandFactory;
import software.ulpgc.es.monolith.domain.model.Customer;
import software.ulpgc.es.monolith.domain.control.customers.*;
import software.ulpgc.es.monolith.domain.io.repository.exceptions.customers.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CommandFactory commandFactory;

    public CustomerController(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    @GetMapping
    public ResponseEntity<List<Customer>> getAllCustomers() {
        final List<Customer>[] resultHolder = new List[1];
        try {
            GetAllCustomersCommand.Output output = result -> resultHolder[0] = result;
            commandFactory.with(Main.NoInput.INSTANCE, output).build("getCustomers").execute();
            return ResponseEntity.ok(resultHolder[0]);
        } catch (CustomersDatabaseException e) {
            return buildError(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (Exception e) {
            return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error retrieving customers.");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomer(@PathVariable int id) {
        final Customer[] resultHolder = new Customer[1];
        try {
            GetCustomerCommand.Input input = () -> id;
            GetCustomerCommand.Output output = result -> resultHolder[0] = result;
            commandFactory.with(input, output).build("getCustomer").execute();
            return Optional.ofNullable(resultHolder[0])
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> buildError(HttpStatus.NOT_FOUND, "Customer not found"));
        } catch (CustomerNotFoundException e) {
            return buildError(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (CustomersDatabaseException e) {
            return buildError(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (Exception e) {
            return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error retrieving customer.");
        }
    }

    @PostMapping
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer) {
        final Customer[] resultHolder = new Customer[1];
        try {
            CreateCustomerCommand.Input input = () -> customer;
            CreateCustomerCommand.Output output = result -> resultHolder[0] = result;
            commandFactory.with(input, output).build("createCustomer").execute();
            return ResponseEntity.status(HttpStatus.CREATED).body(resultHolder[0]);
        } catch (DuplicateCustomerException e) {
            return buildError(HttpStatus.CONFLICT, e.getMessage()); // 409
        } catch (IllegalArgumentException e) {
            return buildError(HttpStatus.BAD_REQUEST, e.getMessage()); // 400
        } catch (CustomersDatabaseException e) {
            return buildError(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()); // 500
        } catch (Exception e) {
            return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error creating customer.");
        }
    }

    @PutMapping
    public ResponseEntity<Customer> updateCustomer(@RequestBody Customer customer) {
        final Customer[] resultHolder = new Customer[1];
        try {
            UpdateCustomerCommand.Input input = () -> customer;
            UpdateCustomerCommand.Output output = result -> resultHolder[0] = result;
            commandFactory.with(input, output).build("updateCustomer").execute();
            return ResponseEntity.ok(resultHolder[0]);
        } catch (CustomerNotFoundException e) {
            return buildError(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (DuplicateCustomerException e) {
            return buildError(HttpStatus.CONFLICT, e.getMessage());
        } catch (IllegalArgumentException e) {
            return buildError(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (CustomersDatabaseException e) {
            return buildError(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (Exception e) {
            return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error updating customer.");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Customer> deleteCustomer(@PathVariable int id) {
        final Customer[] resultHolder = new Customer[1];
        try {
            DeleteCustomerCommand.Input input = () -> id;
            DeleteCustomerCommand.Output output = result -> resultHolder[0] = result;
            commandFactory.with(input, output).build("deleteCustomer").execute();
            return ResponseEntity.ok(resultHolder[0]);
        } catch (CustomerNotFoundException e) {
            return buildError(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (CustomersDatabaseException e) {
            return buildError(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (Exception e) {
            return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error deleting customer.");
        }
    }

    // --- Helper method para construir respuestas de error ---
    private <T> ResponseEntity<T> buildError(HttpStatus status, String message) {
        return ResponseEntity.status(status).body((T) new ErrorResponse(status.value(), message));
    }

    private record ErrorResponse(int status, String message) {}
}
