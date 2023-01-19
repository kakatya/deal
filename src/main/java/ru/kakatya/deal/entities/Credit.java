package ru.kakatya.deal.entities;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import ru.kakatya.deal.dtos.PaymentScheduleElementDto;
import ru.kakatya.deal.entities.enums.CreditStatus;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Table(name = "credit")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class Credit {
    @Id
    @Column(name = "credit_id")
    private long creditId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private int term;

    @Column(name = "monthly_payment", nullable = false)
    private BigDecimal monthlyPayment;

    @Column(nullable = false)
    private BigDecimal rate;

    @Column(nullable = false)
    private BigDecimal psk;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", nullable = false)
    private PaymentScheduleElementDto payment_schedule;

    @Column(name = "insurance_enable", nullable = false)
    private boolean insuranceEnable;

    @Column(name = "salary_client", nullable = false)
    private boolean salaryClient;

    @Enumerated(EnumType.STRING)
    @Column(name = "credit_status", nullable = false)
    private CreditStatus creditStatus;
}
