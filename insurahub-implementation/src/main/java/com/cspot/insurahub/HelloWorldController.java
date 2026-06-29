package com.cspot.insurahub;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.cspot.insurahub.api.HelloApi;

@RestController
public class HelloWorldController implements HelloApi {

    public ResponseEntity<String> helloWorld() {
        return ResponseEntity.ok("Hello, World!");
    }
}
