package ru.kakatya.deal.mappers.interfasemapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ru.kakatya.deal.dtos.ApplicationDto;
import ru.kakatya.deal.entities.Application;

@Mapper(componentModel = "spring", uses = {ClientMapper.class, PassportMapper.class, EmploymentMapper.class})
public interface ApplicationMapper {
    ApplicationMapper INSTANCE = Mappers.getMapper(ApplicationMapper.class);

    ApplicationDto getApplicationDto(Application application);
}
