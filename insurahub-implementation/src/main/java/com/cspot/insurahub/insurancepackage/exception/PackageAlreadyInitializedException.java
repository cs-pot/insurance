package com.cspot.insurahub.insurancepackage.exception;

public class PackageAlreadyInitializedException extends InvalidPackageException {

    public PackageAlreadyInitializedException() {
        super(
                "PACKAGE_ALREADY_INITIALIZED",
                "Package is already initialized"
        );
    }
}
