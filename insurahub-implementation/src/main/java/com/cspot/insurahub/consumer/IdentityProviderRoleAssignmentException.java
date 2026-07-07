package com.cspot.insurahub.consumer;

public class IdentityProviderRoleAssignmentException extends RuntimeException {

    public IdentityProviderRoleAssignmentException(String message) {
        super(message);
    }

    public IdentityProviderRoleAssignmentException(String message, Throwable cause) {
        super(message, cause);
    }

    public IdentityProviderRoleAssignmentException(Throwable cause) {
        super(cause);
    }
}
