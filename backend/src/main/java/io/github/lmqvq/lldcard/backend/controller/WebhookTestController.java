package io.github.lmqvq.lldcard.backend.controller;

import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
public class WebhookTestController {

    @GetMapping("/callback")
    public String handleGetCallback(@RequestParam Map<String, String> params) {
        return "Webhook GET callback received successfully. Params: " + params;
    }

    @PostMapping("/callback")
    public String handlePostCallback(@RequestBody(required = false) Map<String, Object> body, @RequestParam Map<String, String> params) {
        return "Webhook POST callback received successfully.";
    }
}
