package com.cspot.insurahub;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloWorldController {

    @GetMapping("/api/public/hello")
    public String publicHello() {
        return "Hello, World!";
    }

    @GetMapping("/api/private/hello")
    public String privateHello() {
        return "Private Hello, World!";
    }
}
