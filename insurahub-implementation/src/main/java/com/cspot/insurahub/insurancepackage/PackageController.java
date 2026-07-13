package com.cspot.insurahub.insurancepackage;

import com.cspot.insurahub.api.PackagesApi;
import com.cspot.insurahub.model.PostResponse;
import com.cspot.insurahub.model.PostConsumerRequest;
import com.cspot.insurahub.model.PostPackageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PackageController implements PackagesApi {

    private final PackageService service;

    @Override
    @PreAuthorize("hasAuthority('create:packages')")
    public PostResponse postPackage(PostPackageRequest request) {
        return new PostResponse(service.createPackage(request));
    }
}