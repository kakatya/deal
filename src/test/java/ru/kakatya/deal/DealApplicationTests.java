package ru.kakatya.deal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import ru.kakatya.deal.dtos.CreditDTO;
import ru.kakatya.deal.dtos.EmploymentDTO;
import ru.kakatya.deal.dtos.FinishRegistrationRequestDto;
import ru.kakatya.deal.dtos.LoanOfferDTO;
import ru.kakatya.deal.entities.Application;
import ru.kakatya.deal.entities.Client;
import ru.kakatya.deal.entities.Credit;
import ru.kakatya.deal.entities.Passport;
import ru.kakatya.deal.entities.enums.ApplicationStatus;
import ru.kakatya.deal.entities.enums.Gender;
import ru.kakatya.deal.entities.enums.MaritalStatus;
import ru.kakatya.deal.repos.ApplicationRepo;
import ru.kakatya.deal.repos.ClientRepo;
import ru.kakatya.deal.repos.CreditRepo;
import ru.kakatya.deal.sevices.DealService;
import ru.kakatya.deal.sevices.OfferServiceFeignClient;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.Mockito.*;

class DealApplicationTests {
    @Test
    void offerCalculationTest() throws Exception {
        DealService dealService = Mockito.mock(DealService.class);
        doCallRealMethod().when(dealService).chooseOffer(Mockito.any());
        ApplicationRepo applicationRepo = Mockito.mock(ApplicationRepo.class);
        ReflectionTestUtils.setField(dealService, "applicationRepo", applicationRepo);
        Application application = new Application();
        Optional<Application> optionalApplication = Optional.of(application);
        when(applicationRepo.findById(Mockito.anyLong())).thenReturn(optionalApplication);
        ArgumentCaptor<Application> argumentCaptor = ArgumentCaptor.forClass(Application.class);
        dealService.chooseOffer(createLoanOfferDto());
        Mockito.verify(applicationRepo).save(argumentCaptor.capture());
        Application modifiedApplication = argumentCaptor.getValue();

        Assertions.assertEquals(ApplicationStatus.APPROVED, modifiedApplication.getStatus());
        Assertions.assertEquals(modifiedApplication.getAppliedOffer(), createLoanOfferDto());
    }

    @Test
    void registerApplicationTest() throws Exception {
        DealService dealService = mock(DealService.class);

        doCallRealMethod().when(dealService).registerApplication(Mockito.any(), Mockito.anyLong());

        ClientRepo clientRepo = mock(ClientRepo.class);
        CreditRepo creditRepo = mock(CreditRepo.class);
        ApplicationRepo applicationRepo = mock(ApplicationRepo.class);
        OfferServiceFeignClient offerServiceFeignClient = mock(OfferServiceFeignClient.class);

        ReflectionTestUtils.setField(dealService, "clientRepo", clientRepo);
        ReflectionTestUtils.setField(dealService, "creditRepo", creditRepo);
        ReflectionTestUtils.setField(dealService, "applicationRepo", applicationRepo);
        ReflectionTestUtils.setField(dealService, "offerServiceFeignClient", offerServiceFeignClient);

        Application application = new Application();
        Client client = new Client();
        application.setClient(client);
        application.setAppliedOffer(createLoanOfferDto());
        client.setPassport(new Passport());
        client.setEmployment(new EmploymentDTO());

        Optional<Application> optionalApplication = Optional.of(application);
        when(applicationRepo.findById(anyLong())).thenReturn(optionalApplication);

        CreditDTO creditDTO = new CreditDTO();
        creditDTO.setAmount(new BigDecimal("200000"));
        when(offerServiceFeignClient.calculateCredit(any())).thenReturn(ResponseEntity.ok(creditDTO));

        ArgumentCaptor<Credit> creditArgumentCaptor = ArgumentCaptor.forClass(Credit.class);

        dealService.registerApplication(createFinishRegistrationRequestDto(), 1L);
        verify(creditRepo).save(creditArgumentCaptor.capture());
        when(creditRepo.save(any())).thenReturn(creditArgumentCaptor.getValue());

        ArgumentCaptor<Application> applicationArgumentCaptor = ArgumentCaptor.forClass(Application.class);

        verify(applicationRepo).save(applicationArgumentCaptor.capture());
        ArgumentCaptor<Client> clientArgumentCaptor = ArgumentCaptor.forClass(Client.class);
        verify(clientRepo).save(clientArgumentCaptor.capture());
        Assertions.assertEquals(Gender.FEMALE, clientArgumentCaptor.getValue().getGender());
        Assertions.assertEquals(MaritalStatus.DIVORCED, clientArgumentCaptor.getValue().getMaritalStatus());
        Assertions.assertEquals(createEmploymentDTO(), clientArgumentCaptor.getValue().getEmployment());
    }

    private EmploymentDTO createEmploymentDTO() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        EmploymentDTO employmentDTO;
        try {
            File file = new File("src/test/resources/employment.json");
            employmentDTO = objectMapper.readValue(file, EmploymentDTO.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return employmentDTO;
    }

    private LoanOfferDTO createLoanOfferDto() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        LoanOfferDTO loanOfferDTO;
        try {
            File file = new File("src/test/resources/loanOffer.json");
            loanOfferDTO = objectMapper.readValue(file, LoanOfferDTO.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return loanOfferDTO;
    }

    private FinishRegistrationRequestDto createFinishRegistrationRequestDto() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        FinishRegistrationRequestDto finishRegistrationRequestDto;
        try {
            File file = new File("src/test/resources/finishRegistrationRequestDto.json");
            finishRegistrationRequestDto = objectMapper.readValue(file, FinishRegistrationRequestDto.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return finishRegistrationRequestDto;
    }
}
