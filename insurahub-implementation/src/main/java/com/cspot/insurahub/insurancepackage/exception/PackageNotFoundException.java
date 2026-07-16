package com.cspot.insurahub.insurancepackage.exception;

import java.util.UUID;

public class PackageNotFoundException extends RuntimeException {

    public PackageNotFoundException(UUID packageId) {
        super("Package was not found: " + packageId);
    }
}
