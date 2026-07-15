package com.cspot.insurahub.consumer.validation;

import com.cspot.insurahub.consumer.exception.InvalidConsumerPageRequestException;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ConsumerPageableRequestValidator {

    private static final int MIN_PAGE = 0;
    private static final int MIN_SIZE = 1;
    private static final int MAX_SIZE = 100;
    private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of(
            "createdAt",
            "firstName",
            "lastName",
            "email"
    );

    public void validate(String page, String size, String[] sortValues) {
        validatePage(page);
        validateSize(size);
        validateSort(sortValues);
    }

    private static void validatePage(String page) {
        if (page != null && parse(page, "page") < MIN_PAGE) {
            throw new InvalidConsumerPageRequestException("page must be greater than or equal to " + MIN_PAGE);
        }
    }

    private static void validateSize(String size) {
        if (size == null) {
            return;
        }

        int parsedSize = parse(size, "size");
        if (parsedSize < MIN_SIZE) {
            throw new InvalidConsumerPageRequestException("size must be greater than or equal to " + MIN_SIZE);
        }
        if (parsedSize > MAX_SIZE) {
            throw new InvalidConsumerPageRequestException("size must be less than or equal to " + MAX_SIZE);
        }
    }

    private static void validateSort(String[] sortValues) {
        if (sortValues == null) {
            return;
        }

        for (String sortValue : sortValues) {
            validateSortValue(sortValue);
        }
    }

    private static void validateSortValue(String sortValue) {
        String[] parts = sortValue.split(",", -1);
        if (parts.length == 0) {
            throw invalidSortProperty();
        }

        int propertiesEnd = parts.length;
        String lastPart = parts[parts.length - 1].trim();
        if ("asc".equalsIgnoreCase(lastPart) || "desc".equalsIgnoreCase(lastPart)) {
            propertiesEnd--;
        } else if (parts.length > 1 && !ALLOWED_SORT_PROPERTIES.contains(lastPart)) {
            throw new InvalidConsumerPageRequestException("sort direction must be asc or desc");
        }

        if (propertiesEnd == 0) {
            throw invalidSortProperty();
        }

        for (int i = 0; i < propertiesEnd; i++) {
            String property = parts[i].trim();
            if (!ALLOWED_SORT_PROPERTIES.contains(property)) {
                throw invalidSortProperty();
            }
        }
    }

    private static InvalidConsumerPageRequestException invalidSortProperty() {
        return new InvalidConsumerPageRequestException(
                "sort property must be one of: createdAt, firstName, lastName, email");
    }

    private static int parse(String value, String parameterName) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new InvalidConsumerPageRequestException(parameterName + " must be an integer");
        }
    }
}
