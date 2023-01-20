package ru.kakatya.deal.entities;

import lombok.Builder;
import lombok.Data;
import ru.kakatya.deal.entities.enums.ChangeType;

import java.time.LocalDateTime;

@Data
@Builder
public class StatusHistory {
    private String status;
    private LocalDateTime time;
    private ChangeType changeType;
}
