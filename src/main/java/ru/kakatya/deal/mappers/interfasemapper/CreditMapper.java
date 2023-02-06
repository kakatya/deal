package ru.kakatya.deal.mappers.interfasemapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;
import ru.kakatya.deal.dtos.CreditDTO;
import ru.kakatya.deal.entities.Credit;

@Mapper(componentModel = "spring")
@Component
public interface CreditMapper {
    CreditMapper INSTANCE = Mappers.getMapper(CreditMapper.class);

    Credit getCredit(CreditDTO credit);
}
