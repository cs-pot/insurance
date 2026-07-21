package com.cspot.insurahub.payroll;

import java.time.LocalDate;
/**
 * Defines supported payroll frequencies and calculates the minimum inclusive
 * end date required to contain one complete payroll cycle.
 */
public enum Payroll {

    // 7-day payroll cycle
    WEEKLY {
        @Override
        public LocalDate minimumInclusiveEndDate(LocalDate startDate) {
            return startDate.plusDays(6);
        }
    },

    // 14 day payrol cycle
    BI_WEEKLY {
        @Override
        public LocalDate minimumInclusiveEndDate(LocalDate startDate) {
            return startDate.plusDays(13);
        }
    },

    // 1 calendar month
    MONTHLY {
        @Override
        public LocalDate minimumInclusiveEndDate(LocalDate startDate) {
            return startDate.plusMonths(1).minusDays(1);
        }
    },

    // payroll cycle one calendar year
    ANNUALLY {
        @Override
        public LocalDate minimumInclusiveEndDate(LocalDate startDate) {
            return startDate.plusYears(1).minusDays(1);
        }
    };

    /**
     * Calculates the earliest valid inclusive end date for one complete
     * payroll cycle starting from the provided date.
     *
     * @param startDate the first day of the payroll cycle
     * @return the minimum inclusive end date of the payroll cycle
     */

    public abstract LocalDate minimumInclusiveEndDate(LocalDate startDate);
}
