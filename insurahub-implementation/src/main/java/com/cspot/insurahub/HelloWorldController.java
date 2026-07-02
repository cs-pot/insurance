package com.cspot.insurahub;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cspot.insurahub.api.HelloApi;

@RestController
public class HelloWorldController implements HelloApi {

    @Override
    public String helloWorld() {
        return "Hello, World!";
    }

    @GetMapping("/api/public/hello")
    public String publicHello() {
        return helloWorld();
    }

    @GetMapping("/api/private/hello")
    public String privateHello() {
        return "Private Hello, World!";
    }
}
