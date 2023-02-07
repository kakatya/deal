package ru.kakatya.deal.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.kakatya.deal.dtos.ApplicationDto;
import ru.kakatya.deal.dtos.FinishRegistrationRequestDto;
import ru.kakatya.deal.dtos.LoanApplicationRequestDTO;
import ru.kakatya.deal.dtos.LoanOfferDTO;
import ru.kakatya.deal.sevices.DealService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DealControllerTest {
    @Mock
    private DealService dealService;
    @InjectMocks
    private DealController dealController;

    @Test
    void calculationLoanTerms() {
        when(dealService.offerCalculation(any())).thenReturn(new ArrayList<>(Arrays.asList(new LoanOfferDTO(), new LoanOfferDTO(), new LoanOfferDTO(), new LoanOfferDTO())));
        List<LoanOfferDTO> list = dealController.calculationLoanTerms(new LoanApplicationRequestDTO()).getBody();
        assertEquals(4, list.size());
    }

    @Test
    void chooseOffer() {
        dealController.chooseOffer(any());
        verify(dealService, times(1)).chooseOffer(any());
    }

    @Test
    void completeRegistration() {
        dealController.completeRegistration(new FinishRegistrationRequestDto(), 156L);
        verify(dealService, times(1)).registerApplication(any(), any());
    }

    @Test
    void requestSendingDocuments() {
        dealController.requestSendingDocuments(any());
        verify(dealService, times(1)).createDocuments(any());
    }

    @Test
    void requestSigningOfDocuments() {
        dealController.requestSigningOfDocuments(any());
        verify(dealService, times(1)).sendSesCode(any());
    }

    @Test
    void signDocuments() {
        dealController.signDocuments(any(), any());
        verify(dealService, times(1)).checkSesCode(any(), any());
    }

    @Test
    void getApplication() {
        dealController.getApplication(120L);
        verify(dealService, times(1)).getApplication(120L);
        ApplicationDto applicationDto = new ApplicationDto();
        applicationDto.setId(120L);
        when(dealService.getApplication(any())).thenReturn(applicationDto);
        ApplicationDto res = dealController.getApplication(120L).getBody();
        assertEquals(120L, res.getId());
    }

    @Test
    void getAllApplications() {
        dealController.getAllApplications();
        verify(dealService, times(1)).getAllApplication();
    }

    @Test
    void changeApplicationStatus() {
        dealController.requestSendingDocuments(any());
        verify(dealService, times(1)).createDocuments(any());
    }
}