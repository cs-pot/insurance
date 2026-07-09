package com.cspot.insurahub.consumer;

import com.cspot.insurahub.consumer.converter.ConsumerMapper;
import com.cspot.insurahub.consumer.exception.EmailAlreadyInUseException;
import com.cspot.insurahub.consumer.exception.IdentityProviderConflictException;
import com.cspot.insurahub.consumer.exception.IdentityProviderRoleAssignmentException;
import com.cspot.insurahub.consumer.exception.UserCreationException;
import com.cspot.insurahub.model.ConsumerCreateRequest;
import com.cspot.insurahub.model.ConsumerCreationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumerService {

    private final IdentityProviderClient identityProviderClient;
    private final ConsumerRepository consumerRepository;
    private final ConsumerMapper consumerMapper;

    @Transactional
    public ConsumerCreationResponse createConsumer(ConsumerCreateRequest consumerCreateRequest) {
        try {
            return attemptConsumerCreation(consumerCreateRequest);
        } catch (DataIntegrityViolationException | IdentityProviderConflictException e) {
            throw new EmailAlreadyInUseException(e);
        } catch (Exception e) {
            throw new UserCreationException(e);
        }
    }

    private @NonNull ConsumerCreationResponse attemptConsumerCreation(ConsumerCreateRequest consumerCreateRequest) {
        String idpId = null;
        try {
            log.trace("Attempting to register user in identity provider");
            idpId = registerUserWithIdp(consumerCreateRequest);
            log.trace("Attempting to assign user role");
            assignConsumerRoleToUser(idpId);
            log.trace("Attempting to persist user in database");
            return persistConsumerData(consumerCreateRequest, idpId);
        } catch (DataAccessException | IdentityProviderRoleAssignmentException e) {
            log.error("Exception encountered when creating consumer", e);
            deleteUserFromIdp(idpId);
            throw e;
        }
    }

    private void assignConsumerRoleToUser(String idpId) {
        identityProviderClient.addUserRole(idpId, IdpRole.CONSUMER);
    }

    private void deleteUserFromIdp(String idpId) {
        if (idpId != null) {
            identityProviderClient.deleteUser(idpId);
        }
    }

    private ConsumerCreationResponse persistConsumerData(ConsumerCreateRequest consumerCreateRequest,
                                                         String idpId) {
        Consumer consumer = consumerMapper.initializeFromCreateRequest(consumerCreateRequest);
        consumer.setIdpId(idpId);
        consumer = consumerRepository.save(consumer);
        return new ConsumerCreationResponse(consumer.getId());
    }

    private String registerUserWithIdp(ConsumerCreateRequest consumerCreateRequest) {
        return identityProviderClient.registerUser(consumerCreateRequest.getEmail(),
                consumerCreateRequest.getPassword());
    }
}
