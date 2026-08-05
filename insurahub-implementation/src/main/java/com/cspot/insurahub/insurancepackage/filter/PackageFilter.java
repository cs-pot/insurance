package com.cspot.insurahub.insurancepackage.filter;

public record PackageFilter(String name) {

    public String normalizedName() {
        if (name == null || name.isBlank()) {
            return null;
        }

        return name.trim();
    }
}
