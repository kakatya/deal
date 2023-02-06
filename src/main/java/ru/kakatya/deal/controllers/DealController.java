package ru.kakatya.deal.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.kakatya.deal.dtos.ApplicationDto;
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
    public ResponseEntity<Void> chooseOffer(@RequestBody LoanOfferDTO loanOfferDTO) {
        dealService.chooseOffer(loanOfferDTO);
        return ResponseEntity.ok().build();
    }

    @ApiOperation("Завершение регистрации и полный подсчет кредита")
    @PutMapping("/calculate/{applicationId}")
    public ResponseEntity<Void> completeRegistration(@RequestBody FinishRegistrationRequestDto registrationRequestDto, @PathVariable long applicationId) {
        dealService.registerApplication(registrationRequestDto, applicationId);
        return ResponseEntity.ok().build();
    }

    @ApiOperation("Запрос на отправку документов")
    @PostMapping("/document/{applicationId}/send")
    public ResponseEntity<Void> requestSendingDocuments(@PathVariable Long applicationId) {
        dealService.createDocuments(applicationId);
        return ResponseEntity.ok().build();
    }

    @ApiOperation("Запрос на подписание документов")
    @PostMapping("/document/{applicationId}/sign")
    public ResponseEntity<Void> requestSigningOfDocuments(@PathVariable Long applicationId) {
        dealService.sendSesCode(applicationId);
        return ResponseEntity.ok().build();
    }

    @ApiOperation("Подписание документов")
    @PostMapping("/document/{applicationId}/{code}")
    public ResponseEntity<Void> signDocuments(@PathVariable Long applicationId, @PathVariable String code) {
        dealService.checkSesCode(applicationId, code);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/admin/application/{applicationId}")
    public ResponseEntity<ApplicationDto> getApplication(@PathVariable Long applicationId) {
        return ResponseEntity.ok().body(dealService.getApplication(applicationId));
    }

    @GetMapping("/admin/application")
    public ResponseEntity<List<ApplicationDto>> getAllApplications() {
        return ResponseEntity.ok().body(dealService.getAllApplication());
    }

    @PutMapping("/admin/application/{applicationId}/status")
    public ResponseEntity<Void> changeApplicationStatus(@PathVariable Long applicationId) {
        dealService.changeStatusApl(applicationId);
        return ResponseEntity.ok().build();
    }
}
