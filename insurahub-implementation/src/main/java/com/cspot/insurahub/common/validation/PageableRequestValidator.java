package com.cspot.insurahub.common.validation;

import com.cspot.insurahub.common.exception.InvalidPageRequestException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
public class PageableRequestValidator {

    public void validate(String page, String size, String[] sortValues) {
        validate(page, size, sortValues, List.of());
    }

    public void validate(String page, String size, String[] sortValues, Collection<String> allowedSortProperties) {
        validateSort(sortValues, allowedSortProperties);
    }

    private static void validateSort(String[] sortValues, Collection<String> allowedSortProperties) {
        if (sortValues == null) {
            return;
        }

        for (String sortValue : sortValues) {
            validateSortValue(sortValue, allowedSortProperties);
        }
    }

    private static void validateSortValue(String sortValue, Collection<String> allowedSortProperties) {
        if (sortValue.isBlank()) {
            return;
        }

        String[] parts = sortValue.split(",", -1);
        String lastPart = parts[parts.length - 1].trim();
        int propertiesEnd = parts.length;
        if ("asc".equalsIgnoreCase(lastPart) || "desc".equalsIgnoreCase(lastPart)) {
            propertiesEnd--;
        }

        if (parts.length > 1
                && !"asc".equalsIgnoreCase(lastPart)
                && !"desc".equalsIgnoreCase(lastPart)) {
            throw new InvalidPageRequestException("sort direction must be asc or desc");
        }

        for (int i = 0; i < propertiesEnd; i++) {
            String property = parts[i].trim();
            if (property.isEmpty()) {
                continue;
            }
            if (!allowedSortProperties.isEmpty() && !allowedSortProperties.contains(property)) {
                throw new InvalidPageRequestException(
                        "sort property must be one of: " + String.join(", ", allowedSortProperties));
            }
        }
    }
}
