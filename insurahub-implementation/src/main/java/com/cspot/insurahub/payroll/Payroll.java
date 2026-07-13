package com.cspot.insurahub.payroll;

import java.time.LocalDate;

public enum Payroll {
    WEEKLY {
        @Override
        public LocalDate minimumInclusiveEndDate(LocalDate startDate) {
            return startDate.plusDays(6);
        }
    },
    BI_WEEKLY {
        @Override
        public LocalDate minimumInclusiveEndDate(LocalDate startDate) {
            return startDate.plusDays(13);
        }
    },
    MONTHLY {
        @Override
        public LocalDate minimumInclusiveEndDate(LocalDate startDate) {
            return startDate.plusMonths(1).minusDays(1);
        }
    };

    public static final String ALLOWED_VALUES_REGEX = "WEEKLY|BI_WEEKLY|MONTHLY";

    public abstract LocalDate minimumInclusiveEndDate(LocalDate startDate);
}
