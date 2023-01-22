package ru.kakatya.deal.entities;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import ru.kakatya.deal.dtos.PaymentScheduleElementDto;
import ru.kakatya.deal.entities.enums.CreditStatus;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Data
@Builder
@Table(name = "credit")
@NoArgsConstructor
@AllArgsConstructor
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@SequenceGenerator(name = "entityIdGenerator", sequenceName = "credit_id")
public class Credit implements Serializable {
    @Id
    @Column(name = "credit_id", nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long creditId;
    private BigDecimal amount;
    private Integer term;
    @Column(name = "monthly_payment")
    private BigDecimal monthlyPayment;
    private BigDecimal rate;

    private BigDecimal psk;
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "payment_schedule")
    private List<PaymentScheduleElementDto> paymentSchedule;
    @Column(name = "insurance_enable")
    private Boolean insuranceEnable;
    @Column(name = "salary_client")
    private Boolean salaryClient;
    @Enumerated(EnumType.STRING)
    @Column(name = "credit_status")
    private CreditStatus creditStatus;
    @Override
    public String toString() {
        return "Credit{" +
                "creditId=" + creditId +
                ", amount=" + amount +
                ", term=" + term +
                ", monthlyPayment=" + monthlyPayment +
                ", rate=" + rate +
                ", psk=" + psk +
                ", paymentSchedule=" + paymentSchedule +
                ", insuranceEnable=" + insuranceEnable +
                ", salaryClient=" + salaryClient +
                ", creditStatus=" + creditStatus +
                '}';
    }
}
