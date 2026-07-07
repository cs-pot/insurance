package com.cspot.insurahub.consumer.auth0;

import com.auth0.client.mgmt.ManagementApi;
import com.auth0.client.mgmt.core.ManagementApiException;
import com.auth0.client.mgmt.types.CreateUserRequestContent;
import com.auth0.client.mgmt.types.CreateUserResponseContent;
import com.cspot.insurahub.consumer.IdentityProviderClient;
import com.cspot.insurahub.consumer.IdentityProviderRegistrationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Auth0Client implements IdentityProviderClient {

    private final String connectionName;
    private final ManagementApi managementApi;

    public Auth0Client(@Value("${auth0.connection-name}") String connectionName, ManagementApi managementApi) {
        this.connectionName = connectionName;
        this.managementApi = managementApi;
    }

    @Override
    public String registerUser(String email, String password) {
        try {
            CreateUserRequestContent request = CreateUserRequestContent.builder()
                    .connection(connectionName)
                    .email(email)
                    .password(password)
                    .build();
            CreateUserResponseContent createUserResponseContent = managementApi.users().create(request);
            return createUserResponseContent.getUserId()
                    .orElseThrow(() -> new IdentityProviderRegistrationException("Failed to register user with Auth0"));
        } catch (ManagementApiException e) {
            throw new IdentityProviderRegistrationException("Failed to register user with Auth0", e);
        }
    }

    @Override
    public void deleteUser(String idpId) {
        try {
            managementApi.users().delete(idpId);
        } catch (ManagementApiException e) {
            throw new IdentityProviderRegistrationException("Failed to delete user from Auth0", e);
        }
    }
}
