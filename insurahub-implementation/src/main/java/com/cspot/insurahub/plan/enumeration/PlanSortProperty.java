package com.cspot.insurahub.plan.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Getter
@RequiredArgsConstructor
public enum PlanSortProperty {

    CREATED_AT("createdAt"),
    NAME("name"),
    TYPE("type");

    private final String propertyName;

    public static List<String> propertyNames() {
        return Arrays.stream(values())
                .map(PlanSortProperty::getPropertyName)
                .toList();
    }
}
