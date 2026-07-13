package com.cspot.insurahub.consumer.exception;

public class IdentityProviderRoleAssignmentException extends IdentityProviderException {

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
