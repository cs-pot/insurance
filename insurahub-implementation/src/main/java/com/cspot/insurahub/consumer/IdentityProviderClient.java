package com.cspot.insurahub.consumer;

public interface IdentityProviderClient {

    String registerUser(String email, String password);

    void deleteUser(String idpId);
}
