package com.cspot.insurahub.consumer.service;

import com.cspot.insurahub.consumer.entity.Consumer;
import com.cspot.insurahub.consumer.enumeration.IdpRole;
import com.cspot.insurahub.consumer.exception.ConsumerNotFoundException;
import com.cspot.insurahub.consumer.exception.IdentityProviderRoleAssignmentException;
import com.cspot.insurahub.consumer.identity.IdentityProviderClient;
import com.cspot.insurahub.consumer.mapper.ConsumerMapper;
import com.cspot.insurahub.consumer.repository.ConsumerRepository;
import com.cspot.insurahub.model.ConsumerResponse;
import com.cspot.insurahub.model.PostConsumerRequest;
import com.cspot.insurahub.model.PostResponse;
import com.cspot.insurahub.model.PutConsumerRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsumerServiceTest {

    @Mock
    private IdentityProviderClient identityProviderClient;

    @Mock
    private ConsumerRepository consumerRepository;

    @Mock
    private ConsumerMapper consumerMapper;

    @InjectMocks
    private ConsumerService consumerService;

    @Test
    public void shouldGetConsumers() {
        // GIVEN
        Consumer consumer = getConsumer();
        ConsumerResponse listItem = new ConsumerResponse()
                .id(UUID.randomUUID())
                .firstName("First Name")
                .lastName("Last Name")
                .fullName("First Name Last Name")
                .personalId("12345678910")
                .dateOfBirth(LocalDate.of(2026, 7, 7));
        Pageable pageable = PageRequest.of(0, 20, Sort.by("createdAt").ascending());
        when(consumerRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(consumer), PageRequest.of(0, 20), 1));
        when(consumerMapper.toListItemResponse(consumer)).thenReturn(listItem);

        // WHEN
        Page<ConsumerResponse> consumers = consumerService.getConsumers(pageable);

        // THEN
        assertThat(consumers.getContent()).containsExactly(listItem);
        assertThat(consumers.getNumber()).isZero();
        assertThat(consumers.getSize()).isEqualTo(20);
        assertThat(consumers.getTotalElements()).isEqualTo(1);
        assertThat(consumers.getTotalPages()).isEqualTo(1);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(consumerRepository).findAll(pageableCaptor.capture());
        assertThat(pageableCaptor.getValue()).isEqualTo(pageable);
        verify(consumerMapper).toListItemResponse(consumer);
        verifyNoInteractions(identityProviderClient);
    }

    @Test
    public void shouldCreateConsumer() {
        // GIVEN
        PostConsumerRequest createRequest = getPostConsumerRequest();
        Consumer consumer = getConsumer();
        setUpMocksForRepositoryAndMapper(createRequest, consumer);
        when(identityProviderClient.registerUser(createRequest.getEmail(), createRequest.getPassword())).thenReturn(
                "idpId");

        // WHEN
        PostResponse response = consumerService.createConsumer(createRequest);

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
        PostConsumerRequest createRequest = getPostConsumerRequest();
        Consumer consumer = getConsumer();
        setUpMocksForRepositoryAndMapper(createRequest, consumer);
        when(identityProviderClient.registerUser(createRequest.getEmail(), createRequest.getPassword())).thenReturn(
                "idpId");
        doThrow(new IdentityProviderRoleAssignmentException("role assignment failed"))
                .when(identityProviderClient).addUserRole("idpId", IdpRole.CONSUMER);

        // WHEN
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> consumerService.createConsumer(createRequest));

        // THEN
        assertInstanceOf(IdentityProviderRoleAssignmentException.class, exception.getCause());
        verify(consumerRepository).save(any(Consumer.class));
        verify(identityProviderClient).registerUser(createRequest.getEmail(), createRequest.getPassword());
        verify(identityProviderClient).addUserRole("idpId", IdpRole.CONSUMER);
        verify(identityProviderClient).deleteUser("idpId");
    }

    @Test
    public void shouldDeleteUserFromIdpAndThrowWhenDbTransactionFails() {
        // GIVEN
        PostConsumerRequest createRequest = getPostConsumerRequest();
        Consumer consumer = getConsumer();
        setUpMocksForRepositoryAndMapper(createRequest, consumer);
        DataIntegrityViolationException dataAccessException =
                new DataIntegrityViolationException("database unavailable");
        when(identityProviderClient.registerUser(createRequest.getEmail(), createRequest.getPassword())).thenReturn(
                "idpId");
        doThrow(dataAccessException).when(consumerRepository).flush();

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

    @Test
    void shouldThrowNotFoundWhenUpdatingUnknownConsumer() {
        // GIVEN
        UUID consumerId = UUID.randomUUID();
        PutConsumerRequest updateRequest = getConsumerUpdateRequest();
        when(consumerRepository.findById(consumerId)).thenReturn(Optional.empty());

        // WHEN
        assertThrows(ConsumerNotFoundException.class,
                () -> consumerService.updateConsumer(consumerId, updateRequest));

        // THEN
        verify(consumerRepository).findById(consumerId);
        verifyNoMoreInteractions(consumerRepository);
        verifyNoInteractions(consumerMapper, identityProviderClient);
    }

    @Test
    void shouldUpdateConsumer() {
        // GIVEN
        UUID consumerId = UUID.randomUUID();
        Consumer consumer = getConsumer();
        PutConsumerRequest updateRequest = getConsumerUpdateRequest();
        when(consumerRepository.findById(consumerId)).thenReturn(Optional.of(consumer));

        // WHEN
        consumerService.updateConsumer(consumerId, updateRequest);

        // THEN
        verify(consumerMapper).applyUpdateRequest(consumer, updateRequest);
        verify(consumerRepository).save(consumer);
        verifyNoInteractions(identityProviderClient);
    }

    private PutConsumerRequest getConsumerUpdateRequest() {
        return new PutConsumerRequest()
                .firstName("First Name")
                .lastName("Last Name")
                .personalId("12345678910")
                .dateOfBirth(LocalDate.of(2026, 7, 7))
                .address("Address")
                .city("City");
    }

    private void setUpMocksForRepositoryAndMapper(PostConsumerRequest createRequest, Consumer consumer) {
        when(consumerMapper.initializeFromCreateRequest(createRequest)).thenReturn(consumer);
        when(consumerRepository.save(any(Consumer.class))).thenAnswer(invocation -> {
            Consumer argument = invocation.getArgument(0);
            ReflectionTestUtils.setField(argument, "id", UUID.randomUUID());
            return argument;
        });
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

    private PostConsumerRequest getPostConsumerRequest() {
        PostConsumerRequest createRequest = new PostConsumerRequest()
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
