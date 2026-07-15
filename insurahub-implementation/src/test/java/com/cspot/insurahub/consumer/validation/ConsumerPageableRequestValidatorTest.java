package com.cspot.insurahub.consumer.validation;

import com.cspot.insurahub.consumer.exception.InvalidConsumerPageRequestException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConsumerPageableRequestValidatorTest {

    private final ConsumerPageableRequestValidator validator = new ConsumerPageableRequestValidator();

    @Test
    void shouldAcceptValidPageRequest() {
        assertThatCode(() -> validator.validate("0", "20", new String[]{"firstName,asc", "lastName,desc"}))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectNegativePage() {
        assertThatThrownBy(() -> validator.validate("-1", "20", null))
                .isInstanceOf(InvalidConsumerPageRequestException.class)
                .hasMessage("page must be greater than or equal to 0");
    }

    @Test
    void shouldRejectTooLargeSize() {
        assertThatThrownBy(() -> validator.validate("0", "101", null))
                .isInstanceOf(InvalidConsumerPageRequestException.class)
                .hasMessage("size must be less than or equal to 100");
    }

    @Test
    void shouldRejectUnsupportedSortProperty() {
        assertThatThrownBy(() -> validator.validate("0", "20", new String[]{"personalId,asc"}))
                .isInstanceOf(InvalidConsumerPageRequestException.class)
                .hasMessage("sort property must be one of: createdAt, firstName, lastName, email");
    }

    @Test
    void shouldRejectUnsupportedSortDirection() {
        assertThatThrownBy(() -> validator.validate("0", "20", new String[]{"firstName,ascending"}))
                .isInstanceOf(InvalidConsumerPageRequestException.class)
                .hasMessage("sort direction must be asc or desc");
    }
}
