package ru.kakatya.deal.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.kakatya.deal.dtos.FinishRegistrationRequestDto;
import ru.kakatya.deal.dtos.LoanApplicationRequestDTO;
import ru.kakatya.deal.dtos.LoanOfferDTO;
import ru.kakatya.deal.sevices.DealService;

import java.util.List;

@RestController
@RequestMapping("/deal")
@Api(tags = "Контроллер сделки")
public class DealController {
    @Autowired
    private DealService dealService;

    @ApiOperation("Расчет кредитных предложений")
    @PostMapping("/application")
    public ResponseEntity<List<LoanOfferDTO>> calculationLoanTerms(@RequestBody LoanApplicationRequestDTO dto) {
        return ResponseEntity.ok().body(dealService.offerCalculation(dto));
    }

    @ApiOperation("Выбор предложения")
    @PutMapping("/offer")
    public void chooseOffer(@RequestBody LoanOfferDTO loanOfferDTO) {
        dealService.chooseOffer(loanOfferDTO);
    }

    @ApiOperation("Завершение регистрации и полный подсчет кредита")
    @PutMapping("/calculate/{applicationId}")
    public void completeRegistration(@RequestBody FinishRegistrationRequestDto registrationRequestDto, @PathVariable long applicationId) {
        dealService.registerApplication(registrationRequestDto, applicationId);
    }
}
