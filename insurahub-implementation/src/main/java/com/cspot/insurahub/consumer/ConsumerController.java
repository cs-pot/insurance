package com.cspot.insurahub.consumer;

import com.cspot.insurahub.api.ConsumersApi;
import com.cspot.insurahub.model.ConsumerCreateRequest;
import com.cspot.insurahub.model.ConsumerCreationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ConsumerController implements ConsumersApi {

    private final ConsumerService consumerService;

    @Override
    @PreAuthorize("hasAuthority('create:consumers')")
    public ConsumerCreationResponse createConsumer(ConsumerCreateRequest consumerCreateRequest) {
        return consumerService.createConsumer(consumerCreateRequest);
    }
}
