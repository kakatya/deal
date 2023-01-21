package ru.kakatya.deal.dtos;

import lombok.Builder;
import lombok.Data;
import ru.kakatya.deal.entities.enums.EmploymentPosition;
import ru.kakatya.deal.entities.enums.EmploymentStatus;

import java.math.BigDecimal;

@Data
@Builder
public class EmploymentDTO {
    private EmploymentStatus employmentStatus;
    private String employerINN;
    private BigDecimal salary;
    private EmploymentPosition position;
    private int workExperienceTotal;
    private int workExperienceCurrent;
}
