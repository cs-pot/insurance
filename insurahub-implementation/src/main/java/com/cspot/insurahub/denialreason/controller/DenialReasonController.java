package com.cspot.insurahub.denialreason.controller;

import com.cspot.insurahub.api.DenialReasonsApi;
import com.cspot.insurahub.denialreason.service.DenialReasonService;
import com.cspot.insurahub.model.CreateDenialReasonRequest;
import com.cspot.insurahub.model.DenialReasonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DenialReasonController implements DenialReasonsApi {

    private final DenialReasonService denialReasonService;

    @Override
    @PreAuthorize("hasAuthority('view:denial-reasons')")
    public List<DenialReasonResponse> getDenialReasons() {
        return denialReasonService.getDenialReasons();
    }

    @Override
    @PreAuthorize("hasAuthority('create:denial-reasons')")
    @ResponseStatus(HttpStatus.CREATED)
    public DenialReasonResponse createDenialReason(@Valid CreateDenialReasonRequest request) {
        return denialReasonService.createDenialReason(
                request.getLabel(),
                request.getDescription()
        );
    }
}
