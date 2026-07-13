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
    },
    ANNUALLY {
        @Override
        public LocalDate minimumInclusiveEndDate(LocalDate startDate) {
            return startDate.plusYears(1).minusDays(1);
        }
    };

    public abstract LocalDate minimumInclusiveEndDate(LocalDate startDate);
}
