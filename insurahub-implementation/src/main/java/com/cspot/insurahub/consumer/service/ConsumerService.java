package com.cspot.insurahub.consumer.service;

import com.cspot.insurahub.consumer.entity.Consumer;
import com.cspot.insurahub.consumer.enumeration.IdpRole;
import com.cspot.insurahub.consumer.exception.ConsumerNotFoundException;
import com.cspot.insurahub.consumer.exception.EmailAlreadyInUseException;
import com.cspot.insurahub.consumer.exception.IdentityProviderConflictException;
import com.cspot.insurahub.consumer.exception.IdentityProviderRoleAssignmentException;
import com.cspot.insurahub.consumer.exception.UserCreationException;
import com.cspot.insurahub.consumer.identity.IdentityProviderClient;
import com.cspot.insurahub.consumer.mapper.ConsumerMapper;
import com.cspot.insurahub.consumer.repository.ConsumerRepository;
import com.cspot.insurahub.model.ConsumerResponse;
import com.cspot.insurahub.model.PostConsumerRequest;
import com.cspot.insurahub.model.PutConsumerRequest;
import com.cspot.insurahub.model.PostResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumerService {

    private final IdentityProviderClient identityProviderClient;
    private final ConsumerRepository consumerRepository;
    private final ConsumerMapper consumerMapper;

    @Transactional(readOnly = true)
    public Page<ConsumerResponse> getConsumers(Pageable pageable) {
        return consumerRepository.findAll(pageable)
                .map(consumerMapper::toListItemResponse);
    }

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

    @Transactional
    public void updateConsumer(UUID id, PutConsumerRequest updateRequest) {
        Consumer consumer = consumerRepository.findById(id)
                .orElseThrow(() -> new ConsumerNotFoundException("Consumer not found with id: " + id));
        
        consumerMapper.applyUpdateRequest(consumer, updateRequest);
        consumerRepository.save(consumer);
    }

    @Transactional
    public void deleteConsumer(UUID id) {
        log.info("Deleting consumer with ID = {}", id);
        Consumer consumer = consumerRepository.findById(id)
                .orElseThrow(() -> new ConsumerNotFoundException("Consumer not found with id: " + id));

        String deletedBy = getDeletedBy();
        
        consumer.markDeleted(deletedBy);
        consumerRepository.save(consumer);

        deactivateUserInIdp(consumer.getIdpId());
    }

    private String getDeletedBy() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Authentication is required to delete a consumer");
        }
        return authentication.getName();
    }

    private void deactivateUserInIdp(String idpId) {
        try {
            identityProviderClient.deactivateUser(idpId);
        } catch (Exception e) {
            log.error("Failed to deactivate user in identity provider", e);
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
