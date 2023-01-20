package ru.kakatya.deal.sevices;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.kakatya.deal.dtos.LoanApplicationRequestDTO;
import ru.kakatya.deal.dtos.LoanOfferDTO;

import java.util.List;

@FeignClient(name = "ru.kakatya.conveyor", url = "http://127.0.0.1:8080/conveyor")
public interface OfferServiceFeignClient {
    @PostMapping("/offers")
    ResponseEntity<List<LoanOfferDTO>> issueOffer(@RequestBody LoanApplicationRequestDTO dto);
}
