package ru.kakatya.deal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import ru.kakatya.deal.dtos.LoanOfferDTO;
import ru.kakatya.deal.entities.Application;
import ru.kakatya.deal.entities.enums.ApplicationStatus;
import ru.kakatya.deal.repos.ApplicationRepo;
import ru.kakatya.deal.sevices.DealService;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

class DealApplicationTests {
    @Test
    void offerCalculation() {
        DealService dealService = Mockito.mock(DealService.class);
        Mockito.doCallRealMethod().when(dealService).chooseOffer(Mockito.any());
        ApplicationRepo applicationRepo = Mockito.mock(ApplicationRepo.class);
        ReflectionTestUtils.setField(dealService, "applicationRepo", applicationRepo);
        Application application = new Application();
        Optional<Application> optionalApplication = Optional.of(application);
        Mockito.when(applicationRepo.findById(Mockito.anyLong())).thenReturn(optionalApplication);
        ArgumentCaptor<Application> argumentCaptor = ArgumentCaptor.forClass(Application.class);
        dealService.chooseOffer(createLoanOfferDto());
        Mockito.verify(applicationRepo).save(argumentCaptor.capture());
        Application modifiedApplication = argumentCaptor.getValue();

        Assertions.assertEquals(ApplicationStatus.APPROVED, modifiedApplication.getStatus());
        Assertions.assertEquals(modifiedApplication.getAppliedOffer(), createLoanOfferDto());
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

}
