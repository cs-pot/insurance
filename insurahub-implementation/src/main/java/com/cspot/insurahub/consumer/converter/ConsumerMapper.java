package com.cspot.insurahub.consumer.converter;

import com.cspot.insurahub.consumer.Consumer;
import com.cspot.insurahub.model.ConsumerCreateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public abstract class ConsumerMapper {

    public Consumer initializeFromCreateRequest(@NonNull ConsumerCreateRequest consumerCreateRequest) {
        Objects.requireNonNull(consumerCreateRequest, "consumerCreateRequest must not be null");
        return toConsumer(consumerCreateRequest);
    }

    protected abstract Consumer toConsumer(ConsumerCreateRequest consumerCreateRequest);
}
