package com.cspot.insurahub.consumer;

import com.cspot.insurahub.consumer.converter.ConsumerMapper;
import com.cspot.insurahub.model.ConsumerCreateRequest;
import com.cspot.insurahub.model.ConsumerCreationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConsumerServiceImpl implements ConsumerService {

    private final IdentityProviderClient identityProviderClient;
    private final ConsumerRepository consumerRepository;
    private final ConsumerMapper consumerMapper;

    @Override
    @PreAuthorize("hasAuthority('create:consumers')")
    public ConsumerCreationResponse createConsumer(ConsumerCreateRequest consumerCreateRequest) {
        String idpId = null;
        try {
            idpId = registerUserWithIdp(consumerCreateRequest);
            assignConsumerRoleToUser(idpId);
            return persistConsumerData(consumerCreateRequest, idpId);
        } catch (DataAccessException | IdentityProviderRoleAssignmentException e) {
            deleteUserFromIdp(idpId);
            throw new RuntimeException(e);
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
        String idpId = identityProviderClient.registerUser(consumerCreateRequest.getEmail(),
                consumerCreateRequest.getPassword());
        return idpId;
    }
}
