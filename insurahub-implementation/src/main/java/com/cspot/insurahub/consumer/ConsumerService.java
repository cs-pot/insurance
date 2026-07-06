package com.cspot.insurahub.consumer;

import com.cspot.insurahub.model.ConsumerCreateRequest;
import com.cspot.insurahub.model.ConsumerCreationResponse;

public interface ConsumerService {

    ConsumerCreationResponse createConsumer(ConsumerCreateRequest consumerCreateRequest);
}