package ru.kakatya.deal.sevices;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.kakatya.deal.dtos.CreditDTO;
import ru.kakatya.deal.dtos.LoanApplicationRequestDTO;
import ru.kakatya.deal.dtos.LoanOfferDTO;
import ru.kakatya.deal.dtos.ScoringDataDTO;

import java.util.List;

@FeignClient(name = "${service.name}", url = "${service.url}")
public interface OfferServiceFeignClient {
    @PostMapping("/offers")
    ResponseEntity<List<LoanOfferDTO>> issueOffer(@RequestBody LoanApplicationRequestDTO dto);

    @PostMapping("/calculation")
    ResponseEntity<CreditDTO> calculateCredit(@RequestBody ScoringDataDTO dto);
}
