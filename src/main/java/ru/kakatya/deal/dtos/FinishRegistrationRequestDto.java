package ru.kakatya.deal.dtos;

import lombok.Data;
import ru.kakatya.deal.entities.enums.Gender;
import ru.kakatya.deal.entities.enums.MaritalStatus;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class FinishRegistrationRequestDto implements Serializable {
    private Gender gender;
    private MaritalStatus maritalStatus;
    private Integer dependentAmount;
    private LocalDate passportIssueDate;
    private String passportIssueBranch;
    private EmploymentDTO employment;
    private String account;
}
