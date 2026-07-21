package com.cspot.insurahub.common.validation;

import com.cspot.insurahub.common.exception.InvalidPageRequestException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PageableRequestValidatorTest {

    private final PageableRequestValidator validator = new PageableRequestValidator();

    @Test
    void shouldAcceptValidPageRequest() {
        assertThatCode(() -> validator.validate("0", "20", new String[]{"firstName,asc", "lastName,desc"}))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldAcceptUnsupportedSortProperty() {
        assertThatCode(() -> validator.validate("0", "20", new String[]{"personalId,asc"}))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectSortPropertyOutsideAllowedSet() {
        assertThatThrownBy(() -> validator.validate("0", "20", new String[]{"idpId,asc"},
                List.of("createdAt", "firstName", "lastName", "personalId", "dateOfBirth")))
                .isInstanceOf(InvalidPageRequestException.class)
                .hasMessage("sort property must be one of: createdAt, firstName, lastName, personalId, dateOfBirth");
    }

    @Test
    void shouldAcceptBlankSortValue() {
        assertThatCode(() -> validator.validate("0", "20", new String[]{""}))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldAcceptBlankSortPropertyBeforeDirection() {
        assertThatCode(() -> validator.validate("0", "20", new String[]{",asc"}))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectUnsupportedSortDirection() {
        assertThatThrownBy(() -> validator.validate("0", "20", new String[]{"firstName,ascending"}))
                .isInstanceOf(InvalidPageRequestException.class)
                .hasMessage("sort direction must be asc or desc");
    }

    @Test
    void shouldRejectSortDirectionWithTypo() {
        assertThatThrownBy(() -> validator.validate("0", "20", new String[]{"createdAt,descx"}))
                .isInstanceOf(InvalidPageRequestException.class)
                .hasMessage("sort direction must be asc or desc");
    }
}
