package com.cspot.insurahub.insurancepackage;

import com.cspot.insurahub.api.PackagesApi;
import com.cspot.insurahub.model.PackageCreateRequest;
import com.cspot.insurahub.model.PackageCreationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PackageController implements PackagesApi {

    private final PackageService service;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public PackageCreationResponse createPackage(PackageCreateRequest request) {
        return new PackageCreationResponse(service.createPackage(request));
    }
}