package com.cspot.insurahub.consumer.auth0;

import com.auth0.client.mgmt.ManagementApi;
import com.auth0.client.mgmt.core.ManagementApiException;
import com.auth0.client.mgmt.types.CreateUserRequestContent;
import com.auth0.client.mgmt.types.CreateUserResponseContent;
import com.auth0.client.mgmt.users.types.AssignUserRolesRequestContent;
import com.cspot.insurahub.consumer.enumeration.IdpRole;
import com.cspot.insurahub.consumer.exception.IdentityProviderConflictException;
import com.cspot.insurahub.consumer.exception.IdentityProviderRegistrationException;
import com.cspot.insurahub.consumer.exception.IdentityProviderRoleAssignmentException;
import com.cspot.insurahub.consumer.identity.IdentityProviderClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Auth0Client implements IdentityProviderClient {

    private final String connectionName;
    private final String consumerRole;
    private final String adminRole;
    private final ManagementApi managementApi;

    public Auth0Client(@Value("${auth0.connection-name}") String connectionName,
                       @Value("${auth0.role.consumer}") String consumerRole,
                       @Value("${auth0.role.admin}") String adminRole, ManagementApi managementApi) {
        this.connectionName = connectionName;
        this.consumerRole = consumerRole;
        this.adminRole = adminRole;
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
            if (e.statusCode() == 409) {
                throw new IdentityProviderConflictException("Received status 409 from Auth0", e);
            }
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

    @Override
    public void addUserRole(String userId, IdpRole role) {
        try {
            AssignUserRolesRequestContent request = AssignUserRolesRequestContent.builder()
                    .addRoles(getRoleId(role))
                    .build();
            managementApi.users().roles().assign(userId, request);
        } catch (ManagementApiException e) {
            throw new IdentityProviderRoleAssignmentException("Failed to assign role " + role + " to user", e);
        }
    }

    private String getRoleId(IdpRole role) {
        return switch (role) {
            case CONSUMER -> consumerRole;
            case ADMIN -> adminRole;
        };
    }
}
