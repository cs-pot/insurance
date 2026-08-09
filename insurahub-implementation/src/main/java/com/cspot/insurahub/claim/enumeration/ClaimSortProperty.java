package com.cspot.insurahub.claim.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Getter
@RequiredArgsConstructor
public enum ClaimSortProperty {
    CREATED_AT("createdAt"),
    CLAIM_NUMBER("claimNumber"),
    SERVICE_DATE("serviceDate"),
    AMOUNT("amount"),
    STATUS("status");

    private final String propertyName;

    public static List<String> propertyNames() {
        return Arrays.stream(values())
                .map(ClaimSortProperty::getPropertyName)
                .toList();
    }
}
