package com.cspot.insurahub.insurancepackage.controller;

import com.cspot.insurahub.api.PackagesApi;
import com.cspot.insurahub.insurancepackage.service.PackageService;
import com.cspot.insurahub.model.PostResponse;
import com.cspot.insurahub.model.PackageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PackageController implements PackagesApi {

    private final PackageService packageService;

    @Override
    @PreAuthorize("hasAuthority('create:packages')")
    public PostResponse postPackage(PackageRequest request) {
        return packageService.createPackage(request);
    }

    @Override
    @PreAuthorize("hasAuthority('update:packages')")
    public void initializePackage(UUID packageId) {
        packageService.initializePackage(packageId);
    }
}
