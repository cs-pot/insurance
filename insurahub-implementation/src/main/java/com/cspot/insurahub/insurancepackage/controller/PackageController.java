package com.cspot.insurahub.insurancepackage.controller;

import com.cspot.insurahub.api.PackagesApi;
import com.cspot.insurahub.insurancepackage.service.PackageService;
import com.cspot.insurahub.model.PackageRequest;
import com.cspot.insurahub.model.PackageResponse;
import com.cspot.insurahub.model.PostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PackageController implements PackagesApi {

    private final PackageService packageService;

    @Override
    @PreAuthorize("hasAuthority('view:packages')")
    public Page<PackageResponse> getPackages(Pageable pageable) {
        return packageService.getPackages(pageable);
    }

    @Override
    @PreAuthorize("hasAuthority('create:packages')")
    public PostResponse postPackage(PackageRequest request) {
        return packageService.createPackage(request);
    }

    @Override
    @PreAuthorize("hasAuthority('update:packages')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void initializePackage(UUID packageId) {
        packageService.initializePackage(packageId);
    }

    @Override
    @PreAuthorize("hasAuthority('update:packages')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void putPackage(UUID id, PackageRequest packageRequest) {
        packageService.updatePackage(id, packageRequest);
    }
}
