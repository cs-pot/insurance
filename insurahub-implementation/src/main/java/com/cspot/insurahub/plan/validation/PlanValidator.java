package com.cspot.insurahub.plan.validation;

import com.cspot.insurahub.insurancepackage.exception.InvalidPackageException;
import com.cspot.insurahub.model.PlanRequest;
import org.springframework.stereotype.Component;

@Component
public class PlanValidator {

    private static final int MONEY_MIN = 10;
    private static final int MONEY_MAX = 1000;
    private static final int NAME_MAX_LENGTH = 50;
    private static final String ENGLISH_NAME_PATTERN = "^(?=.*[A-Za-z])[A-Za-z ]+$";

    public void validate(PlanRequest request) {
        validateName(request.getName());
        validateMoneyRange(
                request.getContribution(),
                "PLAN_CONTRIBUTION_OUT_OF_RANGE",
                "Contribution must be between 10 and 1000"
        );
        validateMoneyRange(
                request.getElection(),
                "PLAN_ELECTION_OUT_OF_RANGE",
                "Election must be between 10 and 1000"
        );
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidPackageException(
                    "PLAN_NAME_REQUIRED",
                    "Plan name is required"
            );
        }

        if (name.length() > NAME_MAX_LENGTH || !name.matches(ENGLISH_NAME_PATTERN)) {
            throw new InvalidPackageException(
                    "PLAN_NAME_INVALID",
                    "Plan name must contain only English letters and spaces, up to 50 characters"
            );
        }
    }

    private void validateMoneyRange(
            Integer value,
            String code,
            String message
    ) {
        if (value == null) {
            return;
        }

        if (value < MONEY_MIN || value > MONEY_MAX) {
            throw new InvalidPackageException(code, message);
        }
    }
}
