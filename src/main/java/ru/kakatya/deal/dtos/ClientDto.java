package ru.kakatya.deal.dtos;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientDto implements Serializable {
    private String lastName;
    private String firstName;
    private String middleName;
    private LocalDate birthDate;
    private String email;
    private String gender;
    private String maritalStatus;
    private int dependentAmount;
    private String passport;
    private String employment;
    private String account;
}
