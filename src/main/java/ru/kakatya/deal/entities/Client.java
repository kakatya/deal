package ru.kakatya.deal.entities;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import ru.kakatya.deal.dtos.EmploymentDTO;
import ru.kakatya.deal.entities.enums.Gender;
import ru.kakatya.deal.entities.enums.MaritalStatus;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@Entity
@Builder
@Table(name = "client")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@NoArgsConstructor
@AllArgsConstructor
@SequenceGenerator(name = "entityIdGenerator", sequenceName = "client_id")
public class Client implements Serializable {
    @Id
    @GeneratedValue(generator = "entityIdGenerator")
    @Column(name = "client_id", nullable = false, unique = true)
    private long clientId;

    @Column(name = "last_name", nullable = false, length = 30)
    private String lastName;

    @Column(name = "first_name", nullable = false, length = 30)
    private String firstName;

    @Column(name = "middle_name", length = 30)
    private String middleName;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "gender")
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "marital_status")
    @Enumerated(EnumType.STRING)
    private MaritalStatus maritalStatus;

    @Column(name = "Dependent_amount")
    private int dependentAmount;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private Passport passport;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private EmploymentDTO employment;


    @Column(name = "account")
    private String account;

}
