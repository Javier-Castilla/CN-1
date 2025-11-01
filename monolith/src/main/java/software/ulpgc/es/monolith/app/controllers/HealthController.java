package software.ulpgc.es.monolith.app.controllers;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/health")
public class HealthController {
    @GetMapping
    public String getHealth() {
        return "healthy";
    }
}
