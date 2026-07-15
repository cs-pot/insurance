package com.cspot.insurahub.consumer.mapper;

import com.cspot.insurahub.consumer.entity.Consumer;
import com.cspot.insurahub.model.ConsumerResponse;
import com.cspot.insurahub.model.PostConsumerRequest;
import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.Objects;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public abstract class ConsumerMapper {

    public Consumer initializeFromCreateRequest(@NonNull PostConsumerRequest request) {
        Objects.requireNonNull(request, "consumerCreateRequest must not be null");
        return toConsumer(request);
    }

    @Mapping(target = "fullName", expression = "java(consumer.getFirstName() + \" \" + consumer.getLastName())")
    public abstract ConsumerResponse toListItemResponse(Consumer consumer);

    protected abstract Consumer toConsumer(PostConsumerRequest request);
}
