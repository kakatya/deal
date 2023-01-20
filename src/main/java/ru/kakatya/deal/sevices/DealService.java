package ru.kakatya.deal.sevices;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.stereotype.Service;
import ru.kakatya.deal.dtos.LoanApplicationRequestDTO;
import ru.kakatya.deal.dtos.LoanOfferDTO;
import ru.kakatya.deal.entities.Application;
import ru.kakatya.deal.entities.Client;
import ru.kakatya.deal.entities.Passport;
import ru.kakatya.deal.repos.ApplicationRepo;
import ru.kakatya.deal.repos.ClientRepo;

import java.time.LocalDateTime;
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

    private Client createClientEntity(LoanApplicationRequestDTO dto) {
        LOGGER.info("Create a new client and save it to the database.");
        Passport passport = Passport.builder().number(dto.getPassportNumber()).series(dto.getPassportSeries()).build();
        Client client = Client.builder().email(dto.getEmail()).firstName(dto.getFirstName()).lastName(dto.getLastName())
                .middleName(dto.getMiddleName()).birthDate(dto.getBirthdate()).passport(passport).build();
        clientRepo.save(client);
        return client;
    }

    private Application createApplicationEntity(Client client) {
        LOGGER.info("Create a new client and save it to the database.");
        Application application = Application.builder().client(client).creationDate(LocalDateTime.now()).build();
        applicationRepo.save(application);
        return application;
    }
}
