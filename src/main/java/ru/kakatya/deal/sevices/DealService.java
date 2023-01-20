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
        createApplicationEntity(dto, client);
        return offerServiceFeignClient.issueOffer(dto).getBody();
    }

    private Client createClientEntity(LoanApplicationRequestDTO dto) {
        LOGGER.info("Create a new client and save it to the database.");
        Client client = new Client();
        Passport passport = new Passport();
        passport.setNumber(dto.getPassportNumber());
        passport.setSeries(dto.getPassportSeries());
        client.setEmail(dto.getEmail());
        client.setFirstName(dto.getFirstName());
        client.setLastName(dto.getLastName());
        client.setLastName(dto.getLastName());
        client.setEmail(dto.getEmail());
        client.setBirthDate(dto.getBirthdate());
        client.setPassport(passport);
        clientRepo.save(client);
        return client;
    }

    private void createApplicationEntity(LoanApplicationRequestDTO dto, Client client) {
        LOGGER.info("Create a new client and save it to the database.");
        Application application = new Application();
        application.setClient(client);
        application.setCreationDate(LocalDateTime.now());
        applicationRepo.save(application);
    }
}
