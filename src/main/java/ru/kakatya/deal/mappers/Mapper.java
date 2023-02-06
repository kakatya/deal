package ru.kakatya.deal.mappers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.kakatya.deal.dtos.ApplicationDto;
import ru.kakatya.deal.dtos.CreditDTO;
import ru.kakatya.deal.entities.Application;
import ru.kakatya.deal.entities.Credit;
import ru.kakatya.deal.mappers.interfasemapper.*;

@Component
public class Mapper {
    @Autowired
    private ApplicationMapper applicationMapper;
    @Autowired
    private CreditMapper creditMapper;
    @Autowired
    private ClientMapper clientMapper;
    @Autowired
    private EmploymentMapper employmentMapper;
    @Autowired
    private PassportMapper passportMapper;

    public ApplicationDto createApplicationDto(Application application) {
        return applicationMapper.getApplicationDto(application);
    }

    public Credit createCreditEntity(CreditDTO creditDTO) {
        return creditMapper.getCredit(creditDTO);
    }
}
