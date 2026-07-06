package com.cspot.insurahub.consumer;

import com.cspot.insurahub.consumer.converter.ConsumerMapper;
import com.cspot.insurahub.model.ConsumerCreateRequest;
import com.cspot.insurahub.model.ConsumerCreationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
public class ConsumerServiceImpl implements ConsumerService {

    private final IdentityProviderClient identityProviderClient;
    private final ConsumerRepository consumerRepository;
    private final ConsumerMapper consumerMapper;

    @Autowired
    public ConsumerServiceImpl(IdentityProviderClient identityProviderClient, ConsumerRepository consumerRepository,
                               ConsumerMapper consumerMapper) {
        this.identityProviderClient = identityProviderClient;
        this.consumerRepository = consumerRepository;
        this.consumerMapper = consumerMapper;
    }

    @Override
    public ConsumerCreationResponse createConsumer(ConsumerCreateRequest consumerCreateRequest) {
        String idpId = null;
        try {
            idpId = registerUserWithIdp(consumerCreateRequest);
            return persistConsumerData(consumerCreateRequest, idpId);
        } catch (DataAccessException e) {
            deleteUserFromIdp(idpId);
            throw new RuntimeException(e);
        }
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
