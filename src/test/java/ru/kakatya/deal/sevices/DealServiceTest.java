package ru.kakatya.deal.sevices;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import ru.kakatya.deal.dtos.*;
import ru.kakatya.deal.entities.Application;
import ru.kakatya.deal.entities.Client;
import ru.kakatya.deal.entities.Credit;
import ru.kakatya.deal.entities.Passport;
import ru.kakatya.deal.entities.enums.ApplicationStatus;
import ru.kakatya.deal.entities.enums.CreditStatus;
import ru.kakatya.deal.entities.enums.EmploymentStatus;
import ru.kakatya.deal.exceptions.ScoringException;
import ru.kakatya.deal.mappers.Mapper;
import ru.kakatya.deal.repos.ApplicationRepo;
import ru.kakatya.deal.repos.ClientRepo;
import ru.kakatya.deal.repos.CreditRepo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DealServiceTest {
    @Mock
    private OfferServiceFeignClient offerServiceFeignClient;
    @Mock
    private ClientRepo clientRepo;
    @Mock
    private ApplicationRepo applicationRepo;
    @Mock
    private KafkaTemplate<String, EmailMessageDto> kafkaTemplate;
    @Mock
    private CreditRepo creditRepo;
    @Mock
    private Mapper mapper;
    @InjectMocks
    private DealService dealService;

    @Test
    void offerCalculation() {
        Application application = new Application();
        application.setId(120L);
        when(applicationRepo.save(any())).thenReturn(application);
        when(clientRepo.save(any())).thenReturn(new Client());
        when(offerServiceFeignClient.issueOffer(any())).thenReturn(ResponseEntity.ok()
                .body(new ArrayList<>(Arrays.asList(new LoanOfferDTO(), new LoanOfferDTO(), new LoanOfferDTO(), new LoanOfferDTO()))));
        List<LoanOfferDTO> list = dealService.offerCalculation(new LoanApplicationRequestDTO());
        assertEquals(4, list.size());
        for (LoanOfferDTO l : list
        ) {
            assertEquals(120L, l.getApplicationId());
        }
    }

    @Test
    void chooseOffer() {
        LoanOfferDTO loanOfferDTO = new LoanOfferDTO();
        loanOfferDTO.setApplicationId(135L);
        loanOfferDTO.setRequestedAmount(new BigDecimal("123000"));
        Application application = new Application();
        Client client = new Client();
        client.setEmail("example@tr.cd");
        application.setId(135L);
        application.setClient(client);
        loanOfferDTO.setApplicationId(135L);
        when(applicationRepo.findById(135L)).thenReturn(Optional.of(application));
        dealService.chooseOffer(loanOfferDTO);
        verify(applicationRepo, times(2)).save(argThat((application1 ->
                application1.getStatus().name().equals(ApplicationStatus.APPROVED.name())
                        && application1.getAppliedOffer().getRequestedAmount().equals(loanOfferDTO.getRequestedAmount())
        )));
        verify(applicationRepo, times(2)).findById(135L);
        verify(kafkaTemplate, times(1)).send(any(), eq(EmailMessageDto.builder()
                .theme(Theme.FINISH_REGISTRATION)
                .address(client.getEmail())
                .applicationId(135L)
                .build()));
        LoanOfferDTO loanOfferDTOBad = new LoanOfferDTO();
        loanOfferDTOBad.setApplicationId(12L);
        when(applicationRepo.findById(12L)).thenThrow(new NoSuchElementException("Application with id %12 not found"));
        try {
            dealService.chooseOffer(loanOfferDTOBad);
        } catch (NoSuchElementException ex) {
            assertEquals("Application with id %12 not found", ex.getMessage());
        }
    }

    @Test
    void registerApplication() {
        FinishRegistrationRequestDto dto = new FinishRegistrationRequestDto();
        Application application = new Application();
        Client client = new Client();
        LoanOfferDTO loanOfferDTO = new LoanOfferDTO();
        CreditDTO creditDTO = new CreditDTO();
        creditDTO.setAmount(new BigDecimal("123000"));
        loanOfferDTO.setApplicationId(120L);
        loanOfferDTO.setRequestedAmount(new BigDecimal("123000"));
        client.setPassport(Passport.builder().build());
        client.setBirthDate(LocalDate.of(1992, 12, 11));
        dto.setEmployment(EmploymentDTO.builder().employmentStatus(EmploymentStatus.BUSINESS_OWNER)
                .salary(new BigDecimal("120000"))
                .build());
        dto.setDependentAmount(1254);
        application.setStatus(ApplicationStatus.APPROVED);
        application.setId(120L);
        application.setClient(client);
        application.setAppliedOffer(loanOfferDTO);
        when(applicationRepo.findById(120L)).thenReturn(Optional.of(application));
        when(offerServiceFeignClient.calculateCredit(any())).thenReturn(ResponseEntity.ok().body(creditDTO));
        when(mapper.createCreditEntity(any())).thenReturn(new Credit());
        dealService.registerApplication(dto, 120L);
        verify(applicationRepo, times(2)).save(argThat((application1 ->
                application1.getStatus().name().equals(ApplicationStatus.CC_APPROVED.name()) &&
                        application1.getClient().getEmployment().getSalary().equals(new BigDecimal("120000"))
        )));
        verify(creditRepo, times(2)).save(argThat((credit ->
                credit.getCreditStatus().name().equals(CreditStatus.CALCULATED.name()))));
        client.setBirthDate(LocalDate.of(2005, 12, 11));
        try {
            application.setStatus(ApplicationStatus.APPROVED);
            dealService.registerApplication(dto, 120L);
        } catch (ScoringException e) {
            assertEquals("Scoring failed", e.getMessage());
        }

    }

    @Test
    void createDocuments() {
        Application application = new Application();
        Client client = new Client();
        client.setEmail("exm@tr.t");
        application.setStatus(ApplicationStatus.CC_APPROVED);
        application.setClient(client);
        when(applicationRepo.findById(120L)).thenReturn(Optional.of(application));
        dealService.createDocuments(120L);
        verify(applicationRepo, times(1)).save(argThat((application1 ->
                application1.getStatus().name().equals(ApplicationStatus.PREPARE_DOCUMENTS.name()))));
    }

    @Test
    void changeStatusApl() {
        Application application = new Application();
        Client client = new Client();
        client.setEmail("exm@tr.t");
        application.setStatus(ApplicationStatus.PREPARE_DOCUMENTS);
        application.setClient(client);
        when(applicationRepo.findById(120L)).thenReturn(Optional.of(application));
        dealService.changeStatusApl(120L);
        verify(applicationRepo, times(1)).save(argThat(
                (application1 -> application1.getStatus().name().equals(ApplicationStatus.DOCUMENT_CREATED.name()))
        ));

    }

    @Test
    void sendSesCode() {
        Application application = new Application();
        Client client = new Client();
        client.setEmail("exm@tr.t");
        application.setStatus(ApplicationStatus.DOCUMENT_CREATED);
        application.setClient(client);
        when(applicationRepo.findById(120L)).thenReturn(Optional.of(application));
        dealService.sendSesCode(120L);
        verify(applicationRepo, times(1)).save(argThat(
                (application1 -> application1.getSesCode() != null)
        ));
    }

    @Test
    void checkSesCode() {
        Application application = new Application();
        Client client = new Client();
        client.setEmail("exm@tr.t");
        application.setStatus(ApplicationStatus.DOCUMENT_CREATED);
        application.setClient(client);
        application.setSesCode("12548");
        when(applicationRepo.findById(120L)).thenReturn(Optional.of(application));
        dealService.checkSesCode(120L, "12548");
        verify(applicationRepo, times(2)).save(argThat(
                (application1 -> application1.getStatus().name().equals(ApplicationStatus.CREDIT_ISSUED.name()) &&
                        application1.getSesCode().equals("12548"))
        ));
    }

    @Test
    void getApplication() {
        Application application = new Application();
        Client client = new Client();
        client.setEmployment(EmploymentDTO.builder().salary(new BigDecimal("12548")).build());
        client.setPassport(new Passport());
        application.setStatus(ApplicationStatus.DOCUMENT_CREATED);
        application.setClient(client);
        when(applicationRepo.findById(120L)).thenReturn(Optional.of(application));
        when(mapper.createApplicationDto(any())).thenReturn(ApplicationDto.builder().client(new ClientDto()).build());
        ApplicationDto applicationDto = dealService.getApplication(120L);
        Assertions.assertTrue(applicationDto.getClient().getEmployment().contains("12548"));

    }
}