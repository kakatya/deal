package ru.kakatya.deal.entities;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import ru.kakatya.deal.dtos.LoanOfferDTO;
import ru.kakatya.deal.entities.enums.ApplicationStatus;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Application {
    @Id
    @Column(name = "application_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;


    @JoinColumn(name = "client_id", referencedColumnName = "client_id")
    @OneToOne(cascade = CascadeType.ALL)
    private Client client;


    @JoinColumn(name = "credit_id", referencedColumnName = "credit_id")
    @OneToOne(cascade = CascadeType.ALL)
    private Credit credit;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    @Column(name = "creation_date")
    private LocalDateTime creationDate;

    @Type(type = "jsonb")
    @Column(name = "applied_offer", columnDefinition = "jsonb")
    private LoanOfferDTO appliedOffer;

    @Column(name = "sign_date")
    private LocalDateTime signDate;

    @Column(name = "ses_code")
    private String sesCode;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private StatusHistory statusHistory;

}
