package com.cspot.insurahub.plan.validation;

import com.cspot.insurahub.model.PlanRequest;
import com.cspot.insurahub.model.PlanType;
import com.cspot.insurahub.plan.exception.InvalidPlanException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlanValidatorTest {

    private PlanValidator planValidator;

    @BeforeEach
    void setUp() {
        planValidator = new PlanValidator();
    }

    @Test
    void shouldValidatePlanRequestWhenAllFieldsAreValid() {
        PlanRequest request = createPlanRequest("Standard Health", 250, 500);

        assertDoesNotThrow(() -> planValidator.validate(request));
    }

    @Test
    void shouldRejectInvalidPlanName() {
        PlanRequest request = createPlanRequest("Plan!", 250, 500);

        InvalidPlanException exception = assertThrows(
                InvalidPlanException.class,
                () -> planValidator.validate(request)
        );

        assertThat(exception.getCode())
                .isEqualTo("PLAN_NAME_INVALID");
    }

    @Test
    void shouldRejectContributionOutsideAllowedRange() {
        PlanRequest request = createPlanRequest("Standard Health", 9, 500);

        InvalidPlanException exception = assertThrows(
                InvalidPlanException.class,
                () -> planValidator.validate(request)
        );

        assertThat(exception.getCode())
                .isEqualTo("PLAN_CONTRIBUTION_OUT_OF_RANGE");
    }

    @Test
    void shouldRejectElectionOutsideAllowedRange() {
        PlanRequest request = createPlanRequest("Standard Health", 250, 1001);

        InvalidPlanException exception = assertThrows(
                InvalidPlanException.class,
                () -> planValidator.validate(request)
        );

        assertThat(exception.getCode())
                .isEqualTo("PLAN_ELECTION_OUT_OF_RANGE");
    }

    private PlanRequest createPlanRequest(
            String name,
            int contribution,
            int election
    ) {
        return new PlanRequest(
                name,
                PlanType.HEALTH_INSURANCE,
                contribution,
                election
        );
    }
}
