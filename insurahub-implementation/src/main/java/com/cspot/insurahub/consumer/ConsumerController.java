package com.cspot.insurahub.consumer;

import com.cspot.insurahub.api.ConsumersApi;
import com.cspot.insurahub.model.PostConsumerRequest;
import com.cspot.insurahub.model.PostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ConsumerController implements ConsumersApi {

    private final ConsumerService consumerService;

    @Override
    @PreAuthorize("hasAuthority('create:consumers')")
    public PostResponse postConsumer(PostConsumerRequest request) {
        return consumerService.createConsumer(request);
    }
}
