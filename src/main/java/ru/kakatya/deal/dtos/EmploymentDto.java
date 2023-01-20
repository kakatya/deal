package ru.kakatya.deal.dtos;

import lombok.Data;
import ru.kakatya.deal.entities.enums.EmploymentPosition;
import ru.kakatya.deal.entities.enums.EmploymentStatus;

import java.math.BigDecimal;

@Data
public class EmploymentDto {
    private EmploymentStatus status;
    private String employerInn;
    private BigDecimal salary;
    private EmploymentPosition position;
    private int workExperienceTotal;
    private int workExperienceCurrent;
}
