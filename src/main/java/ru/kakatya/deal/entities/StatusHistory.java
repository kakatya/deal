package ru.kakatya.deal.entities;

import lombok.Builder;
import lombok.Data;
import ru.kakatya.deal.entities.enums.ApplicationStatus;
import ru.kakatya.deal.entities.enums.ChangeType;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
public class StatusHistory implements Serializable {
    private ApplicationStatus status;
    private LocalDateTime time;
    private ChangeType changeType;
}
