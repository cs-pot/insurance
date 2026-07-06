package com.cspot.insurahub.consumer.converter;

import com.cspot.insurahub.model.ConsumerCreateRequest;
import com.cspot.insurahub.consumer.Consumer;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

@Component
public class ConsumerMapper {

    public Consumer initializeFromCreateRequest(@NonNull ConsumerCreateRequest consumerCreateRequest) {
        Consumer consumer = new Consumer();
        consumer.setEmail(consumerCreateRequest.getEmail());
        consumer.setFirstName(consumerCreateRequest.getFirstName());
        consumer.setLastName(consumerCreateRequest.getLastName());
        consumer.setPersonalId(consumerCreateRequest.getPersonalId());
        consumer.setDateOfBirth(consumerCreateRequest.getDateOfBirth());
        consumer.setAddress(consumerCreateRequest.getAddress());
        consumer.setCity(consumerCreateRequest.getCity());
        return consumer;
    }
}