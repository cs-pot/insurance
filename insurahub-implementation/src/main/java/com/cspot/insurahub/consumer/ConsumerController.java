package com.cspot.insurahub.consumer;

import com.cspot.insurahub.api.ConsumersApi;
import com.cspot.insurahub.model.ConsumerCreateRequest;
import com.cspot.insurahub.model.ConsumerCreationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
public class ConsumerController implements ConsumersApi {
    
    @Override
    public ConsumerCreationResponse createConsumer(ConsumerCreateRequest consumerCreateRequest) {
        return null;
    }
}
