package ru.kakatya.deal.sevices;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.kakatya.deal.dtos.*;
import ru.kakatya.deal.entities.*;
import ru.kakatya.deal.entities.enums.ApplicationStatus;
import ru.kakatya.deal.entities.enums.ChangeType;
import ru.kakatya.deal.entities.enums.CreditStatus;
import ru.kakatya.deal.exceptions.ScoringException;
import ru.kakatya.deal.mappers.Mapper;
import ru.kakatya.deal.mappers.interfasemapper.ApplicationMapper;
import ru.kakatya.deal.repos.ApplicationRepo;
import ru.kakatya.deal.repos.ClientRepo;
import ru.kakatya.deal.repos.CreditRepo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

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
    @Autowired
    private KafkaTemplate<String, EmailMessageDto> kafkaTemplate;
    @Autowired
    private Mapper mapper;
    @Value("${topics.finish-registr}")
    private String finishRegTopic;
    @Value("${topics.create-doc}")
    private String createDocTopic;
    @Value("${topics.send-doc}")
    private String sendDocTopic;
    @Value("${topics.send-ses}")
    private String sendSesTopic;
    @Value("${topics.credit-issd}")
    private String creditIssuedTopic;
    @Value("${topics.appl-denied}")
    private String applDenied;

    public List<LoanOfferDTO> offerCalculation(LoanApplicationRequestDTO dto) {
        LOGGER.info("Calculation of possible loan conditions");
        Client client = createClientEntity(dto);
        long applicationId = createApplicationEntity(client).getId();
        List<LoanOfferDTO> loanOffers = offerServiceFeignClient.issueOffer(dto).getBody();
        if (loanOffers != null) {
            for (LoanOfferDTO l : loanOffers) {
                l.setApplicationId(applicationId);
            }
        }
        return loanOffers;
    }
    public void chooseOffer(LoanOfferDTO loanOfferDTO) throws NoSuchElementException {
        applicationRepo.findById(loanOfferDTO.getApplicationId()).ifPresentOrElse(application -> {
                    LOGGER.info("Set Application status: {}", ApplicationStatus.APPROVED);
                    LOGGER.info("Set Change type: {}", ChangeType.AUTOMATIC);
                    changeStatus(application, ApplicationStatus.APPROVED, ChangeType.AUTOMATIC);
                    application.setAppliedOffer(loanOfferDTO);
                    applicationRepo.save(application);
                    LOGGER.info("Send message to dossier with topic: {}; applicationId: {}", Theme.FINISH_REGISTRATION.name(), loanOfferDTO.getApplicationId());
                    kafkaTemplate.send(finishRegTopic, createEmailMessageDto(loanOfferDTO.getApplicationId(), Theme.FINISH_REGISTRATION));
                },
                () -> {
                    throw new NoSuchElementException(String.format("Application with id %s not found", loanOfferDTO.getApplicationId()));
                });
    }

    public void registerApplication(FinishRegistrationRequestDto dto, Long applicationId) throws ScoringException, NoSuchElementException {
        applicationRepo.findById(applicationId).ifPresentOrElse(application -> {
                    if (!application.getStatus().name().equals(ApplicationStatus.APPROVED.name())) {
                        throw new NoSuchElementException(String.format("Application with status %s not found", ApplicationStatus.APPROVED.name()));
                    }
                    registerClient(dto, application.getClient());
                    ScoringDataDTO scoringDataDTO = createScoringData(application);
                    LOGGER.info("Calculate credit");
                    CreditDTO creditDTO = offerServiceFeignClient.calculateCredit(scoringDataDTO).getBody();
                    Credit credit;
                    if (creditDTO.getAmount() == null) {
                        application.setStatus(ApplicationStatus.CC_DENIED);
                        LOGGER.error("Scoring failed");
                        changeStatus(application, ApplicationStatus.CC_DENIED, ChangeType.AUTOMATIC);
                        kafkaTemplate.send(Theme.APPLICATION_DENIED.name(),
                                createEmailMessageDto(applicationId,
                                        Theme.APPLICATION_DENIED));
                        throw new ScoringException("Scoring failed");
                    }
                    credit = mapper.createCreditEntity(creditDTO);
                    LOGGER.info("update credit status");
                    credit.setCreditStatus(CreditStatus.CALCULATED);
                    creditRepo.save(credit);
                    application.setCredit(creditRepo.save(credit));
                    applicationRepo.save(application);
                    changeStatus(application, ApplicationStatus.CC_APPROVED, ChangeType.AUTOMATIC);
                    kafkaTemplate.send(createDocTopic,
                            createEmailMessageDto(applicationId, Theme.CREATE_DOCUMENTS));
                },
                () -> {
                    throw new NoSuchElementException(String.format("Application with id %s not found", applicationId));
                });
    }

    public void createDocuments(Long applicationId) throws NoSuchElementException {
        LOGGER.info("Send message to dossier with topic: {}; applicationId: {}", Theme.SEND_DOCUMENTS, applicationId);
        applicationRepo.findById(applicationId).ifPresentOrElse(application -> {
                    if (!application.getStatus().name().equals(ApplicationStatus.CC_APPROVED.name())) {
                        throw new NoSuchElementException(String.format("Application with status %s not found", ApplicationStatus.CC_APPROVED.name()));
                    }
                    changeStatus(application, ApplicationStatus.PREPARE_DOCUMENTS, ChangeType.AUTOMATIC);
                },
                () -> {
                    throw new NoSuchElementException(String.format("Application with id %s not found", applicationId));
                });
        kafkaTemplate.send(sendDocTopic, createEmailMessageDto(applicationId, Theme.SEND_DOCUMENTS));
    }

    public void changeStatusApl(Long applicationId) throws NoSuchElementException {
        applicationRepo.findById(applicationId).ifPresentOrElse(application -> {
                    if (!application.getStatus().name().equals(ApplicationStatus.PREPARE_DOCUMENTS.name())) {
                        throw new NoSuchElementException(String.format("Application with status %s not found", ApplicationStatus.PREPARE_DOCUMENTS.name()));
                    }
                    changeStatus(application, ApplicationStatus.DOCUMENT_CREATED, ChangeType.AUTOMATIC);
                },
                () -> {
                    throw new NoSuchElementException(String.format("Application with id %s not found", applicationId));
                });
    }

    public void sendSesCode(Long applicationId) throws NoSuchElementException {
        LOGGER.info("Send message to dossier with topic: {}; applicationId: {}", Theme.SEND_SES, applicationId);
        String sesCode = String.valueOf(1000 + Math.random() * 9000);
        applicationRepo.findById(applicationId).ifPresentOrElse(application -> {
                    if (!application.getStatus().name().equals(ApplicationStatus.DOCUMENT_CREATED.name())) {
                        throw new NoSuchElementException(String.format("Application with status %s not found", ApplicationStatus.DOCUMENT_CREATED.name()));
                    }
                    application.setSesCode(sesCode);
                    applicationRepo.save(application);
                },
                () -> {
                    throw new NoSuchElementException(String.format("Application with id %s not found", applicationId));
                });
        kafkaTemplate.send(sendSesTopic, createEmailMessageDto(applicationId, Theme.SEND_SES));
    }

    public void checkSesCode(Long applicationId, String sesCode) throws NoSuchElementException {
        applicationRepo.findById(applicationId).ifPresentOrElse(application -> {
                    if (!application.getStatus().name().equals(ApplicationStatus.DOCUMENT_CREATED.name())) {
                        throw new NoSuchElementException(String.format("Application with status %s not found", ApplicationStatus.DOCUMENT_CREATED.name()));
                    }
                    boolean result = application.getSesCode().equals(sesCode);
                    if (result) {
                        kafkaTemplate.send(creditIssuedTopic, createEmailMessageDto(applicationId, Theme.CREDIT_ISSUED));
                        changeStatus(application, ApplicationStatus.DOCUMENT_SIGNED, ChangeType.AUTOMATIC);
                        LOGGER.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!CREDIT ISSUED!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                        changeStatus(application, ApplicationStatus.CREDIT_ISSUED, ChangeType.AUTOMATIC);
                    } else {
                        kafkaTemplate.send(applDenied, createEmailMessageDto(applicationId, Theme.APPLICATION_DENIED));
                        changeStatus(application, ApplicationStatus.CLIENT_DENIED, ChangeType.AUTOMATIC);
                    }
                },
                () -> {
                    throw new NoSuchElementException(String.format("Application with id %s not found", applicationId));
                });
    }

    public ApplicationDto getApplication(Long applicationId) throws NoSuchElementException {
        Application application = applicationRepo.findById(applicationId).orElseThrow(() -> {
            throw new NoSuchElementException(String.format("Application with id %s not found", applicationId));
        });
        if (application.getStatus().name().equals(ApplicationStatus.CLIENT_DENIED.name()) ||
                application.getStatus().name().equals(ApplicationStatus.CC_DENIED.name())) {
            throw new NoSuchElementException(String.format("Application with id %s was denied", applicationId));
        }
        LOGGER.info("Get application with ID: {}", applicationId);
        ApplicationDto applicationDto = mapper.createApplicationDto(application);
        applicationDto.getClient().setEmployment(application.getClient().getEmployment().toString());
        applicationDto.getClient().setPassport(application.getClient().getPassport().toString());
        return applicationDto;
    }

    public List<ApplicationDto> getAllApplication() {
        List<ApplicationDto> applications = new ArrayList<>();
        LOGGER.info("Get all application");
        applicationRepo.findAll().forEach(application -> {
            applications.add(ApplicationMapper.INSTANCE.getApplicationDto(application));
        });
        return applications;
    }

    private void changeStatus(Application application, ApplicationStatus status, ChangeType changeType) {
        LOGGER.info("update application status: {}", status.name());
        application.setStatus(status);
        if (application.getStatusHistory() == null) {
            application.setStatusHistory(new ArrayList<>());
        }
        application.getStatusHistory().add(StatusHistory.builder()
                .status(status)
                .changeType(changeType)
                .time(LocalDateTime.now())
                .build());
        applicationRepo.save(application);
    }

    private EmailMessageDto createEmailMessageDto(Long applicationId, Theme theme) {
        return EmailMessageDto.builder()
                .address(findClientEmail(applicationId))
                .theme(theme)
                .applicationId(applicationId)
                .build();
    }

    private String findClientEmail(Long applicationId) throws NoSuchElementException {
        Application application = applicationRepo.findById(applicationId).orElseThrow(() -> {
            throw new NoSuchElementException(String.format("Application with id %s not found", applicationId));
        });
        LOGGER.info("Find client Email");
        return application.getClient().getEmail();
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
