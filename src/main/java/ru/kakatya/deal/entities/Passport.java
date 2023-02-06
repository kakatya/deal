package ru.kakatya.deal.entities;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Passport implements Serializable {
    private String series;
    private String number;
    private String issueBranch;
    private LocalDate issueDate;

    @Override
    public String toString() {
        return "series=" + series + '\n' +
                "number=" + number + '\n' +
                "issueBranch=" + issueBranch + '\n' +
                "issueDate=" + issueDate + "\n";
    }

}
