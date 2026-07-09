package com.cspot.insurahub.consumer;

import com.cspot.insurahub.consumer.converter.ConsumerMapper;
import com.cspot.insurahub.consumer.exception.IdentityProviderRoleAssignmentException;
import com.cspot.insurahub.model.ConsumerCreateRequest;
import com.cspot.insurahub.model.ConsumerCreationResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.dao.DataIntegrityViolationException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsumerServiceImplTest {

    @Mock
    private IdentityProviderClient identityProviderClient;

    @Mock
    private ConsumerRepository consumerRepository;

    @Mock
    private ConsumerMapper consumerMapper;

    @InjectMocks
    private ConsumerServiceImpl consumerService;

    @Test
    public void shouldCreateConsumer() {
        // GIVEN
        ConsumerCreateRequest createRequest = getConsumerCreateRequest();
        Consumer consumer = getConsumer();
        when(identityProviderClient.registerUser(createRequest.getEmail(), createRequest.getPassword())).thenReturn(
                "idpId");
        when(consumerMapper.initializeFromCreateRequest(createRequest)).thenReturn(consumer);
        when(consumerRepository.save(any(Consumer.class))).thenAnswer(invocation -> {
            Consumer argument = invocation.getArgument(0);
            argument.setId(UUID.randomUUID());
            return argument;
        });

        // WHEN
        ConsumerCreationResponse response = consumerService.createConsumer(createRequest);

        // THEN
        verify(identityProviderClient).registerUser(createRequest.getEmail(), createRequest.getPassword());
        verify(identityProviderClient).addUserRole(consumer.getIdpId(), IdpRole.CONSUMER);
        verify(consumerRepository).save(any(Consumer.class));
        verify(identityProviderClient, never()).deleteUser(consumer.getIdpId());
        assertEquals(consumer.getId(), response.getId());
    }

    @Test
    public void shouldDeleteUserFromIdpAndThrowWhenRoleAssignmentFails() {
        // GIVEN
        ConsumerCreateRequest createRequest = getConsumerCreateRequest();
        when(identityProviderClient.registerUser(createRequest.getEmail(), createRequest.getPassword())).thenReturn(
                "idpId");
        doThrow(new IdentityProviderRoleAssignmentException("role assignment failed"))
                .when(identityProviderClient).addUserRole("idpId", IdpRole.CONSUMER);

        // WHEN
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> consumerService.createConsumer(createRequest));

        // THEN
        assertInstanceOf(IdentityProviderRoleAssignmentException.class, exception.getCause());
        verify(identityProviderClient).registerUser(createRequest.getEmail(), createRequest.getPassword());
        verify(identityProviderClient).addUserRole("idpId", IdpRole.CONSUMER);
        verify(identityProviderClient).deleteUser("idpId");
        verifyNoInteractions(consumerRepository);
    }

    @Test
    public void shouldDeleteUserFromIdpAndThrowWhenDbTransactionFails() {
        // GIVEN
        ConsumerCreateRequest createRequest = getConsumerCreateRequest();
        Consumer consumer = getConsumer();
        DataIntegrityViolationException dataAccessException =
                new DataIntegrityViolationException("database unavailable");
        when(identityProviderClient.registerUser(createRequest.getEmail(), createRequest.getPassword())).thenReturn(
                "idpId");
        when(consumerMapper.initializeFromCreateRequest(createRequest)).thenReturn(consumer);
        when(consumerRepository.save(any(Consumer.class))).thenThrow(dataAccessException);

        // WHEN
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> consumerService.createConsumer(createRequest));

        // THEN
        assertSame(dataAccessException, exception.getCause());
        verify(identityProviderClient).registerUser(createRequest.getEmail(), createRequest.getPassword());
        verify(identityProviderClient).addUserRole("idpId", IdpRole.CONSUMER);
        verify(identityProviderClient).deleteUser("idpId");
        verify(consumerRepository).save(any(Consumer.class));
    }

    private Consumer getConsumer() {
        Consumer consumer = new Consumer();
        consumer.setEmail("email@email.org");
        consumer.setIdpId("idpId");
        consumer.setFirstName("First Name");
        consumer.setLastName("Last Name");
        consumer.setPersonalId("12345678910");
        consumer.setDateOfBirth(LocalDate.of(2026, 07, 07));
        consumer.setAddress("Address");
        consumer.setCity("City");
        return consumer;
    }

    private ConsumerCreateRequest getConsumerCreateRequest() {
        ConsumerCreateRequest createRequest = new ConsumerCreateRequest()
                .email("email@email.org")
                .password("SecurePassword123")
                .firstName("First Name")
                .lastName("Last Name")
                .personalId("12345678910")
                .dateOfBirth(LocalDate.of(2026, 07, 07))
                .address("Address")
                .city("City");
        return createRequest;
    }
}
