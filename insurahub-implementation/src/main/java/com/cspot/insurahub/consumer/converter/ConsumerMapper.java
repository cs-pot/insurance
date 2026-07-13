package com.cspot.insurahub.consumer.converter;

import com.cspot.insurahub.consumer.Consumer;
import com.cspot.insurahub.model.PostConsumerRequest;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public abstract class ConsumerMapper {

    public Consumer initializeFromCreateRequest(@NonNull PostConsumerRequest request) {
        Objects.requireNonNull(request, "consumerCreateRequest must not be null");
        return toConsumer(request);
    }

    protected abstract Consumer toConsumer(PostConsumerRequest request);
}
