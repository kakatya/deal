package ru.kakatya.deal.sevices;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.stereotype.Service;
import ru.kakatya.deal.dtos.*;
import ru.kakatya.deal.entities.*;
import ru.kakatya.deal.entities.enums.ApplicationStatus;
import ru.kakatya.deal.entities.enums.ChangeType;
import ru.kakatya.deal.entities.enums.CreditStatus;
import ru.kakatya.deal.exceptions.ScoringException;
import ru.kakatya.deal.repos.ApplicationRepo;
import ru.kakatya.deal.repos.ClientRepo;
import ru.kakatya.deal.repos.CreditRepo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@EnableFeignClients
public class DealService {
    private static final Logger LOGGER = LogManager.getLogger(DealService.class);
    @Autowired
    private ClientRepo clientRepo;
    @Autowired
    private ApplicationRepo applicationRepo;

    @Autowired
    private CreditRepo creditRepo;
    @Autowired
    private OfferServiceFeignClient offerServiceFeignClient;

    public List<LoanOfferDTO> offerCalculation(LoanApplicationRequestDTO dto) {
        LOGGER.info("Calculation of possible loan conditions");
        Client client = createClientEntity(dto);
        long applicationId = createApplicationEntity(client).getId();
        List<LoanOfferDTO> loanOffers = offerServiceFeignClient.issueOffer(dto).getBody();
        for (LoanOfferDTO l : loanOffers) {
            l.setApplicationId(applicationId);
        }
        return loanOffers;
    }

    public void chooseOffer(LoanOfferDTO loanOfferDTO) {
        LOGGER.info("Set Application status: {}", ApplicationStatus.APPROVED);
        LOGGER.info("Set Change type: {}", ChangeType.AUTOMATIC);


        applicationRepo.findById(loanOfferDTO.getApplicationId()).ifPresent(application -> {
            if (application.getStatusHistory() == null) {
                application.setStatusHistory(new ArrayList<>());
            }
            application.getStatusHistory().add(StatusHistory.builder()
                    .status(ApplicationStatus.APPROVED)
                    .changeType(ChangeType.AUTOMATIC)
                    .time(LocalDateTime.now())
                    .build());
            application.setStatus(ApplicationStatus.APPROVED);
            application.setAppliedOffer(loanOfferDTO);
            applicationRepo.save(application);
        });
    }

    public void registerApplication(FinishRegistrationRequestDto dto, Long applicationId) throws ScoringException {
        applicationRepo.findById(applicationId).ifPresent(application -> {
            registerClient(dto, application.getClient());
            ScoringDataDTO scoringDataDTO = createScoringData(application);
            LOGGER.info("Calculate credit");
            CreditDTO creditDTO = offerServiceFeignClient.calculateCredit(scoringDataDTO).getBody();
            Credit credit;
            if (creditDTO.getAmount()==null) {
                LOGGER.error("Scoring failed");
                throw new ScoringException("Scoring failed");
            }
            credit = createCredit(creditDTO);
            LOGGER.info("update credit status");
            credit.setCreditStatus(CreditStatus.CALCULATED);
            application.setCredit(creditRepo.save(credit));
            LOGGER.info("update application status");
            application.setStatus(ApplicationStatus.CC_APPROVED);
            if (application.getStatusHistory() == null) {
                application.setStatusHistory(new ArrayList<>());
            }
            application.getStatusHistory().add(StatusHistory.builder()
                    .status(ApplicationStatus.CC_APPROVED)
                    .changeType(ChangeType.AUTOMATIC)
                    .time(LocalDateTime.now())
                    .build());
            applicationRepo.save(application);
        });
    }

    private ScoringDataDTO createScoringData(Application application) {
        LOGGER.info("crate scoringData");
        return ScoringDataDTO.builder()
                .amount(application.getAppliedOffer().getRequestedAmount())
                .term(application.getAppliedOffer().getTerm())
                .firstName(application.getClient().getFirstName())
                .lastName(application.getClient().getLastName())
                .middleName(application.getClient().getMiddleName())
                .gender(application.getClient().getGender())
                .birthdate(application.getClient().getBirthDate())
                .passportSeries(application.getClient().getPassport().getSeries())
                .passportNumber(application.getClient().getPassport().getNumber())
                .passportIssueDate(application.getClient().getPassport().getIssueDate())
                .passportIssueBranch(application.getClient().getPassport().getIssueBranch())
                .maritalStatus(application.getClient().getMaritalStatus())
                .dependentAmount(application.getClient().getDependentAmount())
                .account(application.getClient().getAccount())
                .isSalaryClient(application.getAppliedOffer().getIsSalaryClient())
                .isInsuranceEnabled(application.getAppliedOffer().getIsInsuranceEnabled())
                .employment(EmploymentDTO.builder()
                        .employerINN(application.getClient().getEmployment().getEmployerINN())
                        .employmentStatus(application.getClient().getEmployment().getEmploymentStatus())
                        .position(application.getClient().getEmployment().getPosition())
                        .salary(application.getClient().getEmployment().getSalary())
                        .workExperienceCurrent(application.getClient().getDependentAmount())
                        .workExperienceTotal(application.getClient().getDependentAmount())
                        .build())
                .build();
    }

    private Credit createCredit(CreditDTO creditDTO) {
        LOGGER.info("Create Credit Entity");
        return Credit.builder()
                .psk(creditDTO.getPsk())
                .amount(creditDTO.getAmount())
                .salaryClient(creditDTO.getIsSalaryClient())
                .insuranceEnable(creditDTO.getIsInsuranceEnabled())
                .paymentSchedule(creditDTO.getPaymentSchedule())
                .monthlyPayment(creditDTO.getMonthlyPayment())
                .rate(creditDTO.getRate())
                .term(creditDTO.getTerm())
                .build();
    }

    private void registerClient(FinishRegistrationRequestDto dto, Client client) {
        client.getPassport().setIssueBranch(dto.getPassportIssueBranch());
        client.getPassport().setIssueDate(dto.getPassportIssueDate());
        client.setEmployment(EmploymentDTO.builder()
                .workExperienceTotal(dto.getEmployment().getWorkExperienceTotal())
                .workExperienceCurrent(dto.getEmployment().getWorkExperienceCurrent())
                .salary(dto.getEmployment().getSalary())
                .position(dto.getEmployment().getPosition())
                .employmentStatus(dto.getEmployment().getEmploymentStatus())
                .employerINN(dto.getEmployment().getEmployerINN())
                .build());
        client.setGender(dto.getGender());
        client.setMaritalStatus(dto.getMaritalStatus());
        client.setAccount(dto.getAccount());
        client.setDependentAmount(dto.getDependentAmount());
        LOGGER.info("Final client registration");
        clientRepo.save(client);
    }

    private Client createClientEntity(LoanApplicationRequestDTO dto) {
        LOGGER.info("Create a new client and save it to the database.");
        Passport passport = Passport.builder()
                .number(dto.getPassportNumber())
                .series(dto.getPassportSeries())
                .build();
        Client client = Client.builder()
                .email(dto.getEmail())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .middleName(dto.getMiddleName())
                .birthDate(dto.getBirthdate())
                .passport(passport)
                .build();
        return clientRepo.save(client);
    }

    private Application createApplicationEntity(Client client) {
        LOGGER.info("Create a new client and save it to the database.");
        Application application = Application.builder()
                .client(client)
                .creationDate(LocalDateTime.now())
                .build();

        return applicationRepo.save(application);
    }
}
