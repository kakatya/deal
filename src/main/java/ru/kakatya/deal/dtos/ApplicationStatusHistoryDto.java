package ru.kakatya.deal.dtos;

import lombok.Builder;
import lombok.Data;
import ru.kakatya.deal.entities.enums.ApplicationStatus;
import ru.kakatya.deal.entities.enums.ChangeType;

import java.time.LocalDateTime;

@Data
@Builder
public class ApplicationStatusHistoryDto {
    private ApplicationStatus status;
    private LocalDateTime time;
    private ChangeType changeType;
}
