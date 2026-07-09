package com.cspot.insurahub.consumer.auth0;

import com.auth0.client.mgmt.ManagementApi;
import com.auth0.client.mgmt.UsersClient;
import com.auth0.client.mgmt.core.ManagementApiException;
import com.auth0.client.mgmt.types.CreateUserRequestContent;
import com.auth0.client.mgmt.types.CreateUserResponseContent;
import com.auth0.client.mgmt.users.RolesClient;
import com.auth0.client.mgmt.users.types.AssignUserRolesRequestContent;
import com.cspot.insurahub.consumer.exception.IdentityProviderRegistrationException;
import com.cspot.insurahub.consumer.exception.IdentityProviderRoleAssignmentException;
import com.cspot.insurahub.consumer.IdpRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Auth0ClientTest {

    private static final String CONNECTION_NAME = "Username-Password-Authentication";
    private static final String CONSUMER_ROLE_ID = "role-consumer";
    private static final String ADMIN_ROLE_ID = "role-admin";

    @Mock
    private ManagementApi managementApi;

    @Mock
    private UsersClient usersClient;

    @Mock
    private RolesClient rolesClient;

    private Auth0Client auth0Client;

    @BeforeEach
    void setUp() {
        auth0Client = new Auth0Client(CONNECTION_NAME, CONSUMER_ROLE_ID, ADMIN_ROLE_ID, managementApi);
        lenient().when(managementApi.users()).thenReturn(usersClient);
        lenient().when(usersClient.roles()).thenReturn(rolesClient);
    }

    @Test
    void shouldRegisterUser() {
        CreateUserResponseContent response = CreateUserResponseContent.builder()
                .userId("auth0|user-123")
                .build();
        when(usersClient.create(any(CreateUserRequestContent.class))).thenReturn(response);

        String userId = auth0Client.registerUser("email@email.org", "SecurePassword123");

        ArgumentCaptor<CreateUserRequestContent> captor = ArgumentCaptor.forClass(CreateUserRequestContent.class);
        verify(usersClient).create(captor.capture());
        assertEquals("auth0|user-123", userId);
        assertEquals(CONNECTION_NAME, captor.getValue().getConnection());
        assertEquals("email@email.org", captor.getValue().getEmail().orElseThrow());
        assertEquals("SecurePassword123", captor.getValue().getPassword().orElseThrow());
    }

    @Test
    void shouldThrowRegistrationExceptionWhenAuth0SendsNoId() {
        when(usersClient.create(any(CreateUserRequestContent.class))).thenReturn(
                CreateUserResponseContent.builder().build());

        IdentityProviderRegistrationException exception = assertThrows(
                IdentityProviderRegistrationException.class,
                () -> auth0Client.registerUser("email@email.org", "SecurePassword123"));

        assertEquals("Failed to register user with Auth0", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void shouldThrowRegistrationExceptionWhenAuth0ThrowsException() {
        ManagementApiException apiException = new ManagementApiException("boom", 500, null);
        when(usersClient.create(any(CreateUserRequestContent.class))).thenThrow(apiException);

        IdentityProviderRegistrationException exception = assertThrows(
                IdentityProviderRegistrationException.class,
                () -> auth0Client.registerUser("email@email.org", "SecurePassword123"));

        assertEquals("Failed to register user with Auth0", exception.getMessage());
        assertInstanceOf(ManagementApiException.class, exception.getCause());
        assertEquals(apiException, exception.getCause());
    }

    @Test
    void shouldThrowDeletionExceptionWhenAuth0ThrowsException() {
        ManagementApiException apiException = new ManagementApiException("boom", 500, null);
        doThrow(apiException).when(usersClient).delete("auth0|user-123");

        IdentityProviderRegistrationException exception = assertThrows(
                IdentityProviderRegistrationException.class,
                () -> auth0Client.deleteUser("auth0|user-123"));

        assertEquals("Failed to delete user from Auth0", exception.getMessage());
        assertEquals(apiException, exception.getCause());
    }

    @Test
    void shouldAssignConsumerUserRole() {
        auth0Client.addUserRole("auth0|user-123", IdpRole.CONSUMER);

        ArgumentCaptor<AssignUserRolesRequestContent> captor =
                ArgumentCaptor.forClass(AssignUserRolesRequestContent.class);
        verify(rolesClient).assign(eq("auth0|user-123"), captor.capture());
        assertEquals(1, captor.getValue().getRoles().size());
        assertEquals(CONSUMER_ROLE_ID, captor.getValue().getRoles().get(0));
    }

    @Test
    void shouldAssignAdminUserRole() {
        auth0Client.addUserRole("auth0|user-123", IdpRole.ADMIN);

        ArgumentCaptor<AssignUserRolesRequestContent> captor =
                ArgumentCaptor.forClass(AssignUserRolesRequestContent.class);
        verify(rolesClient).assign(eq("auth0|user-123"), captor.capture());
        assertEquals(1, captor.getValue().getRoles().size());
        assertEquals(ADMIN_ROLE_ID, captor.getValue().getRoles().get(0));
    }

    @Test
    void shouldThrowRoleAssignmentExceptionWhenAuth0ThrowsException() {
        ManagementApiException apiException = new ManagementApiException("boom", 500, null);
        doThrow(apiException).when(rolesClient).assign(any(), any(AssignUserRolesRequestContent.class));

        IdentityProviderRoleAssignmentException exception = assertThrows(
                IdentityProviderRoleAssignmentException.class,
                () -> auth0Client.addUserRole("auth0|user-123", IdpRole.CONSUMER));

        assertEquals("Failed to assign role CONSUMER to user", exception.getMessage());
        assertEquals(apiException, exception.getCause());
    }
}
