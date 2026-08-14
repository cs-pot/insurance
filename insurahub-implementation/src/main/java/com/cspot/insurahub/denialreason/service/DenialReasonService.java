package com.cspot.insurahub.denialreason.service;

import com.cspot.insurahub.denialreason.entity.DenialReason;
import com.cspot.insurahub.denialreason.mapper.DenialReasonMapper;
import com.cspot.insurahub.denialreason.repository.DenialReasonRepository;
import com.cspot.insurahub.model.DenialReasonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DenialReasonService {

    private final DenialReasonRepository denialReasonRepository;
    private final DenialReasonMapper denialReasonMapper;

    @Transactional(readOnly = true)
    public List<DenialReasonResponse> getDenialReasons() {
        return denialReasonRepository.findAll().stream()
                .map(denialReasonMapper::toResponse)
                .toList();
    }

    @Transactional
    public DenialReasonResponse createDenialReason(String title, String description) {
        DenialReason reason = DenialReason.builder()
                .title(title)
                .description(description)
                .build();
        return denialReasonMapper.toResponse(denialReasonRepository.save(reason));
    }
}
