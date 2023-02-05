package ru.kakatya.deal.mappers.interfasemapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;
import ru.kakatya.deal.dtos.EmploymentDTO;

@Mapper(componentModel = "spring")
@Component
public interface EmploymentMapper {
    EmploymentMapper INSTANCE = Mappers.getMapper(EmploymentMapper.class);

    String getString(EmploymentDTO employmentDTO);
}
