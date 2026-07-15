package com.cspot.insurahub.consumer;

import com.cspot.insurahub.consumer.converter.ConsumerMapper;
import com.cspot.insurahub.consumer.exception.EmailAlreadyInUseException;
import com.cspot.insurahub.consumer.exception.IdentityProviderConflictException;
import com.cspot.insurahub.consumer.exception.IdentityProviderRoleAssignmentException;
import com.cspot.insurahub.consumer.exception.UserCreationException;
import com.cspot.insurahub.model.PostConsumerRequest;
import com.cspot.insurahub.model.PostResponse;
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
    public PostResponse createConsumer(PostConsumerRequest request) {
        try {
            return attemptConsumerCreation(request);
        } catch (DataIntegrityViolationException | IdentityProviderConflictException e) {
            throw new EmailAlreadyInUseException(e);
        } catch (Exception e) {
            throw new UserCreationException(e);
        }
    }

    private @NonNull PostResponse attemptConsumerCreation(PostConsumerRequest consumerCreateRequest) {
        String idpId = null;
        Consumer consumer = null;
        try {
            log.debug("Attempting to persist user in database");
            consumer = persistConsumerData(consumerCreateRequest);
            log.debug("Attempting to register user in identity provider");
            idpId = registerUserWithIdp(consumerCreateRequest);
            log.debug("Attempting to assign user role");
            assignConsumerRoleToUser(idpId);
            log.debug("Attempting to assign idpId to consumer in the database");
            assignIdpIdInDatabase(consumer, idpId);
            return new PostResponse(consumer.getId());
        } catch (DataAccessException | IdentityProviderRoleAssignmentException e) {
            log.error("Exception encountered when creating consumer", e);
            deleteUserFromIdp(idpId);
            throw e;
        }
    }

    private void assignIdpIdInDatabase(Consumer consumer, String idpId) {
        consumer.setIdpId(idpId);
        consumerRepository.flush();
    }

    private void assignConsumerRoleToUser(String idpId) {
        identityProviderClient.addUserRole(idpId, IdpRole.CONSUMER);
    }

    private void deleteUserFromIdp(String idpId) {
        if (idpId != null) {
            identityProviderClient.deleteUser(idpId);
        }
    }

    private Consumer persistConsumerData(PostConsumerRequest request) {
        Consumer consumer = consumerMapper.initializeFromCreateRequest(request);
        consumer = consumerRepository.save(consumer);
        return consumer;
    }

    private String registerUserWithIdp(PostConsumerRequest consumerCreateRequest) {
        return identityProviderClient.registerUser(consumerCreateRequest.getEmail(),
                consumerCreateRequest.getPassword());
    }
}
