package com.cspot.insurahub.consumer.controller;

import com.cspot.insurahub.api.ConsumersApi;
import com.cspot.insurahub.consumer.service.ConsumerService;
import com.cspot.insurahub.model.ConsumerResponse;
import com.cspot.insurahub.model.PostConsumerRequest;
import com.cspot.insurahub.model.PutConsumerRequest;
import com.cspot.insurahub.model.PostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ConsumerController implements ConsumersApi {

    private final ConsumerService consumerService;

    @Override
    @PreAuthorize("hasAuthority('view:consumers')")
    public Page<ConsumerResponse> getConsumers(Pageable pageable) {
        return consumerService.getConsumers(pageable);
    }

    @Override
    @PreAuthorize("hasAuthority('create:consumers')")
    public PostResponse postConsumer(PostConsumerRequest request) {
        return consumerService.createConsumer(request);
    }

    @Override
    @PreAuthorize("hasAuthority('update:consumers')")
    public void putConsumer(UUID id, PutConsumerRequest request) {
        consumerService.updateConsumer(id, request);
    }

    @Override
    @PreAuthorize("hasAuthority('delete:consumers')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteConsumer(UUID id) {
        consumerService.deleteConsumer(id);
    }
}
