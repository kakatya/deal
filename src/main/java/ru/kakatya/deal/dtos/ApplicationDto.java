package ru.kakatya.deal.dtos;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationDto implements Serializable {
    private Long id;
    private ClientDto client;
    private CreditDTO credit;
    private String status;
    private LocalDateTime creationDate;
    private LoanOfferDTO appliedOffer;
    private LocalDateTime signDate;
    private String sesCode;
}
