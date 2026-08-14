package com.cspot.insurahub.denialreason.service;

import com.cspot.insurahub.denialreason.entity.DenialReason;
import com.cspot.insurahub.denialreason.mapper.DenialReasonMapper;
import com.cspot.insurahub.denialreason.repository.DenialReasonRepository;
import com.cspot.insurahub.model.DenialReasonResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DenialReasonServiceTest {

    @Mock
    private DenialReasonRepository denialReasonRepository;

    @Mock
    private DenialReasonMapper denialReasonMapper;

    @Test
    void shouldCreateCustomDenialReason() {
        UUID id = UUID.randomUUID();
        DenialReason savedReason = DenialReason.builder()
                .id(id)
                .title("Provider not eligible")
                .description("The provider is not eligible under the current plan.")
                .build();
        when(denialReasonRepository.save(any(DenialReason.class))).thenReturn(savedReason);
        DenialReasonResponse mappedResponse = new DenialReasonResponse()
                .id(id)
                .title(savedReason.getTitle())
                .description(savedReason.getDescription());
        when(denialReasonMapper.toResponse(savedReason)).thenReturn(mappedResponse);

        DenialReasonResponse response = new DenialReasonService(denialReasonRepository, denialReasonMapper)
                .createDenialReason(
                        savedReason.getTitle(),
                        savedReason.getDescription()
                );

        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getTitle()).isEqualTo(savedReason.getTitle());
        assertThat(response.getDescription()).isEqualTo(savedReason.getDescription());
        verify(denialReasonRepository).save(any(DenialReason.class));
    }
}
