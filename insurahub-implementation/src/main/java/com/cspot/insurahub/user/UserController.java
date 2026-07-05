package com.cspot.insurahub.user;

import org.springframework.web.bind.annotation.RestController;

import com.cspot.insurahub.api.UsersApi;
import com.cspot.insurahub.model.UserCreateRequest;

import jakarta.validation.Valid;

@RestController
public class UserController implements UsersApi {

    @Override
    public void createUser(@Valid UserCreateRequest userCreateRequest) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createUser'");
    }
}
