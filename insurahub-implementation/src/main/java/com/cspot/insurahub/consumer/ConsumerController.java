package com.cspot.insurahub.consumer;

import com.cspot.insurahub.api.ConsumersApi;
import com.cspot.insurahub.model.ConsumerCreateRequest;
import com.cspot.insurahub.model.ConsumerCreationResponse;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConsumerController implements ConsumersApi {

    private final ConsumerService consumerService;

    public ConsumerController(ConsumerService consumerService) {
        this.consumerService = consumerService;
    }

    @Override
    public ConsumerCreationResponse createConsumer(ConsumerCreateRequest consumerCreateRequest) {
        return consumerService.createConsumer(consumerCreateRequest);
    }
}
