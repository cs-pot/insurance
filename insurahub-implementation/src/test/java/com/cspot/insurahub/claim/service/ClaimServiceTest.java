package com.cspot.insurahub.claim.service;

import com.cspot.insurahub.claim.entity.Claim;
import com.cspot.insurahub.claim.entity.Receipt;
import com.cspot.insurahub.claim.enumeration.ClaimStatus;
import com.cspot.insurahub.claim.repository.ClaimRepository;
import com.cspot.insurahub.claim.repository.ReceiptRepository;
import com.cspot.insurahub.claim.storage.PostgresReceiptStorage;
import com.cspot.insurahub.common.exception.ResourceNotFoundException;
import com.cspot.insurahub.consumer.entity.Consumer;
import com.cspot.insurahub.consumer.repository.ConsumerRepository;
import com.cspot.insurahub.insurancepackage.entity.InsurancePackage;
import com.cspot.insurahub.model.PlanType;
import com.cspot.insurahub.model.PostClaimRequest;
import com.cspot.insurahub.model.PostResponse;
import com.cspot.insurahub.payroll.Payroll;
import com.cspot.insurahub.plan.entity.InsurancePlan;
import com.cspot.insurahub.plan.repository.InsurancePlanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private ReceiptRepository receiptRepository;

    @Mock
    private ConsumerRepository consumerRepository;

    @Mock
    private InsurancePlanRepository insurancePlanRepository;

    @Mock
    private PostgresReceiptStorage receiptStorage;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private ClaimService claimService;

    @Test
    void shouldCreateClaim() {
        UUID consumerId = UUID.randomUUID();
        Consumer consumer = consumer(consumerId);

        UUID planId = UUID.randomUUID();
        InsurancePlan plan = insurancePlan(planId);

        PostClaimRequest request = claimRequest(consumerId, planId);

        when(consumerRepository.findById(consumerId))
                .thenReturn(Optional.of(consumer));

        when(insurancePlanRepository.findById(planId))
                .thenReturn(Optional.of(plan));

        UUID claimId = UUID.randomUUID();
        doAnswer(invocation -> {
            Claim claim = invocation.getArgument(0);
            ReflectionTestUtils.setField(claim, "id", claimId);
            return claim;
        }).when(claimRepository).save(any(Claim.class));

        Receipt receipt = mock(Receipt.class);
        when(receipt.getId()).thenReturn(UUID.randomUUID());

        when(receiptStorage.store(any(Claim.class), same(multipartFile)))
                .thenReturn(receipt);

        PostResponse response = claimService.createClaim(request, multipartFile);

        assertThat(response.getId()).isEqualTo(claimId);

        verify(consumerRepository).findById(consumerId);
        verify(insurancePlanRepository).findById(planId);

        ArgumentCaptor<Claim> claimCaptor = ArgumentCaptor.forClass(Claim.class);
        verify(claimRepository).save(claimCaptor.capture());
        Claim savedClaim = claimCaptor.getValue();

        assertThat(savedClaim.getEmployee()).isSameAs(consumer);
        assertThat(savedClaim.getPlan()).isSameAs(plan);
        assertThat(savedClaim.getServiceDate()).isEqualTo(request.getServiceDate());
        assertThat(savedClaim.getAmount()).isEqualByComparingTo(request.getAmount());
        assertThat(savedClaim.getStatus()).isEqualTo(ClaimStatus.PENDING);

        verify(receiptStorage).store(same(savedClaim), same(multipartFile));
        verifyNoMoreInteractions(receiptRepository);
    }

    @Test
    void shouldThrowWhenEmployeeDoesNotExist() {
        UUID consumerId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        PostClaimRequest request = claimRequest(consumerId, planId);

        when(consumerRepository.findById(consumerId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> claimService.createClaim(request, multipartFile)
        );

        assertThat(exception.getMessage())
                .isEqualTo("Consumer not found with id: " + consumerId);

        verify(consumerRepository).findById(consumerId);
        verifyNoInteractions(
                insurancePlanRepository,
                claimRepository,
                receiptRepository,
                receiptStorage
        );
    }

    @Test
    void shouldThrowWhenPlanDoesNotExist() {
        UUID consumerId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        Consumer consumer = consumer(consumerId);
        PostClaimRequest request = claimRequest(consumerId, planId);

        when(consumerRepository.findById(consumerId))
                .thenReturn(Optional.of(consumer));
        when(insurancePlanRepository.findById(planId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> claimService.createClaim(request, multipartFile)
        );

        assertThat(exception.getMessage())
                .isEqualTo("Insurance plan not found with id: " + planId);

        verify(consumerRepository).findById(consumerId);
        verify(insurancePlanRepository).findById(planId);
        verifyNoInteractions(claimRepository, receiptRepository, receiptStorage);
    }

    @Test
    void shouldReturnReceiptContent() throws IOException {
        UUID receiptId = UUID.randomUUID();
        byte[] content = "receipt".getBytes();
        Receipt receipt = new Receipt(
                claim(),
                "receipt.pdf",
                "application/pdf",
                (long) content.length,
                content
        );

        when(receiptRepository.findById(receiptId))
                .thenReturn(Optional.of(receipt));

        Resource resource = claimService.getReceipt(receiptId);

        assertThat(resource.getInputStream().readAllBytes())
                .containsExactly(content);

        verify(receiptRepository).findById(receiptId);
        verifyNoInteractions(
                consumerRepository,
                insurancePlanRepository,
                claimRepository,
                receiptStorage
        );
    }

    @Test
    void shouldThrowWhenReceiptDoesNotExist() {
        UUID receiptId = UUID.randomUUID();

        when(receiptRepository.findById(receiptId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> claimService.getReceipt(receiptId)
        );

        assertThat(exception.getMessage())
                .isEqualTo("Receipt not found with id: " + receiptId);

        verify(receiptRepository).findById(receiptId);
        verifyNoInteractions(
                consumerRepository,
                insurancePlanRepository,
                claimRepository,
                receiptStorage
        );
    }

    private PostClaimRequest claimRequest(UUID consumerId, UUID planId) {
        return new PostClaimRequest()
                .employeeId(consumerId)
                .planId(planId)
                .serviceDate(LocalDate.of(2026, 7, 10))
                .amount(BigDecimal.valueOf(123.45));
    }

    private Claim claim() {
        return new Claim(
                consumer(UUID.randomUUID()),
                insurancePlan(UUID.randomUUID()),
                LocalDate.of(2026, 7, 10),
                BigDecimal.valueOf(123.45)
        );
    }

    private Consumer consumer(UUID consumerId) {
        Consumer consumer = new Consumer();
        ReflectionTestUtils.setField(consumer, "id", consumerId);
        return consumer;
    }

    private InsurancePlan insurancePlan(UUID planId) {
        InsurancePlan plan = new InsurancePlan(
                insurancePackage(),
                "Plan",
                PlanType.HEALTH_INSURANCE,
                BigDecimal.valueOf(250),
                BigDecimal.valueOf(500)
        );
        ReflectionTestUtils.setField(plan, "id", planId);
        return plan;
    }

    private InsurancePackage insurancePackage() {
        return new InsurancePackage(
                "Package",
                Payroll.MONTHLY,
                LocalDate.now(),
                LocalDate.now().plusMonths(1)
        );
    }
}
