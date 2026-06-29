package com.cspot.insurahub;

import org.springframework.web.bind.annotation.RestController;

import com.cspot.insurahub.api.HelloApi;

@RestController
public class HelloWorldController implements HelloApi {

    public String helloWorld() {
        return "Hello, World!";
    }
}
