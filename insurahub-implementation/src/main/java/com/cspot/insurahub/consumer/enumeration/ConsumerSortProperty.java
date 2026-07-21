package com.cspot.insurahub.consumer.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Getter
@RequiredArgsConstructor
public enum ConsumerSortProperty {
    CREATED_AT("createdAt"),
    FIRST_NAME("firstName"),
    LAST_NAME("lastName"),
    PERSONAL_ID("personalId"),
    DATE_OF_BIRTH("dateOfBirth");

    private final String propertyName;

    public static List<String> propertyNames() {
        return Arrays.stream(values())
                .map(ConsumerSortProperty::getPropertyName)
                .toList();
    }
}
