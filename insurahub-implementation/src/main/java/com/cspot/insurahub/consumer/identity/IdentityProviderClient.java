package com.cspot.insurahub.consumer.identity;

import com.cspot.insurahub.consumer.enumeration.IdpRole;

public interface IdentityProviderClient {

    String registerUser(String email, String password);

    void deleteUser(String idpId);

    void addUserRole(String userId, IdpRole roles);
}
