package ru.kakatya.deal.entities;

import lombok.Data;
import ru.kakatya.deal.entities.enums.ChangeType;

import java.sql.Timestamp;

@Data
public class StatusHistory {
    private String status;
    private Timestamp time;
    private ChangeType changeType;
}
